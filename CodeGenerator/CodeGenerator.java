/*
 * File:    CodeGenerator.java
 * Author:  Rajesh Gopidi
 * PID:     720367703
 * Course : COMP520
 */

package miniJava.CodeGenerator;

import miniJava.AbstractSyntaxTrees.*;
import miniJava.SyntacticAnalyzer.SourcePosition;
import miniJava.AbstractSyntaxTrees.Package;
import miniJava.ContextualAnalyzer.*;
import mJAM.*;
import mJAM.Machine.Op;
import mJAM.Machine.Reg;
import mJAM.Machine.Prim;

public class CodeGenerator implements Visitor<Integer, Integer>
{
    private boolean debug = false, secondWalk = false;
    private boolean isMethodCall = false, isPrintCall = false;
    int labelMain = 0, patchMe = 0;
    int ST = 0;

    public CodeGenerator() 
    {
        Machine.initCodeGen();
    }

    public void generateCode (AST ast)
    {
        ast.visit(this, Integer.valueOf(0));
        secondWalk = true;
        ast.visit(this, Integer.valueOf(0));
        Machine.emit(Op.LOADL, -1);
        Machine.emit(Op.CALL, Reg.CB, labelMain);
        Machine.emit(Op.HALT, 0, 0, 0);
    }   
    
    // Package
    public Integer visitPackage(Package prog, Integer arg)
    {
        ClassDeclList cl = prog.classDeclList;

        for (ClassDecl c: prog.classDeclList) {
            c.visit(this, Integer.valueOf(0));
        }
        return (Integer.valueOf(VOIDRSIZE));
    }
    
    // Declarations
    public Integer visitClassDecl(ClassDecl clas, Integer arg)
    {
        int methodOffset = 0, methodLabel = 0;
        if (!secondWalk) {
            /* 
             * format of class object
             *      static fields
             *      super class
             *      no of methods
             *      method pointers
             */
            for (FieldDecl f: clas.fieldDeclList) {
                if (f.isStatic) {
                    f.storage.offset = ST;
                    Machine.emit(Op.PUSH,1);
                    Machine.emit(Op.LOADL,0);
                    Machine.emit(Op.STORE, Reg.SB, ST);
                    ST++;        
                }
            }
            // storing the offset of the classobject
            clas.storage.offset = ST;
            Machine.emit(Op.LOADL, -1);
            // superclass, no of methods
            Machine.emit(Op.LOADL, clas.methodDeclList.size());
            if (clas.methodDeclList.size() > 0)
                Machine.emit(Op.PUSH, clas.methodDeclList.size());
            ST += 2;
            for (MethodDecl m: clas.methodDeclList) {
                m.storage.offset = methodOffset;
                ST++;
                methodOffset++;
            }
        } else {
            for (MethodDecl m: clas.methodDeclList) {
                methodLabel = Machine.nextInstrAddr(); 
                Machine.emit(Op.LOADL, methodLabel + 3);
                Machine.emit(Op.STORE, Reg.SB, clas.storage.offset + 2 + 
                             m.storage.offset);
                if (clas.containsMain && m.name.equals("main")) {
                    // label of main method
                    labelMain = Machine.nextInstrAddr() + 1;
                }
                patchMe = Machine.nextInstrAddr();
                Machine.emit(Op.JUMP, Reg.CB, 0);
                m.visit(this, Integer.valueOf(0));
                Machine.patch(patchMe, Machine.nextInstrAddr()); 
            }   
        }
        return (Integer.valueOf(VOIDRSIZE));
    } 

    public Integer visitFieldDecl(FieldDecl f, Integer arg)
    {
        return (Integer.valueOf(VOIDRSIZE));
    }

    public Integer visitMethodDecl(MethodDecl m, Integer arg)
    {
        int offset = 3;

        if (debug)
            System.out.println("in method declaration: "+ m.name);
    
        ParameterDeclList pdl = m.parameterDeclList;
        int length = pdl.size();
        for (ParameterDecl pd: pdl) {
            pd.storage.offset = -length;
            length--;
        } 
          
        StatementList sl = m.statementList;

        for (Statement s: sl) {
            offset += s.visit(this, Integer.valueOf(offset)).intValue();
        }
        //when returning subtract 3 offset and pop those many elements from the stack
        if (m.returnExp != null) {
            int pushCount = m.returnExp.visit(this, Integer.valueOf(offset));
            Machine.emit(Op.RETURN, pushCount, 0, m.parameterDeclList.size());
        } else {
        //when returning subtract 3 offset and pop those many elements from the stack
            Machine.emit(Op.RETURN, 0, 0, m.parameterDeclList.size());
        }
    
        // useless, but doing it for convention
        return (Integer.valueOf(VOIDRSIZE));
    }
    
    public Integer visitParameterDecl(ParameterDecl pd, Integer arg) 
    {
        return (Integer.valueOf(VOIDRSIZE));
    } 
    
    public Integer visitVarDecl(VarDecl vd, Integer arg)
    {
        int size = 0;

        vd.storage.offset = arg.intValue();
        size = vd.type.visit(this, arg).intValue();
        vd.storage.size = size;
        Machine.emit(Op.PUSH, size);
    
        return (Integer.valueOf(size));
    }
 
    // Statements
    public Integer visitBlockStmt(BlockStmt stmt, Integer arg)
    {
        int offset = arg.intValue();
        int pushCount = 0;

        StatementList sl = stmt.sl;
        for (Statement s: sl) {
             pushCount += s.visit(this, Integer.valueOf(offset));
             offset += pushCount;
        }
    
        if (pushCount > 0)
            Machine.emit(Op.POP, pushCount);

        return (Integer.valueOf(VOIDRSIZE));
    }
    
    public Integer visitVardeclStmt (VarDeclStmt stmt, Integer arg)
    {
        int pushCount = stmt.varDecl.visit(this, arg).intValue();

        // A a = b | methodcall()..
        stmt.initExp.visit(this, Integer.valueOf((arg.intValue()) + pushCount));
        // Ignoring the value pushed onto the stack as it will be removed by STORE
        Machine.emit(Op.STORE, Reg.LB, stmt.varDecl.storage.offset); 
        return (Integer.valueOf(pushCount));
    }
    
    public Integer visitAssignStmt (AssignStmt stmt, Integer arg)
    {

        System.out.println("Assignment statement");
        int op = LOCALREF, offset = 0;
        isLHS = true;
        int pushCount = stmt.ref.visit(this, arg).intValue();
        op = OP;
        isLHS = false;
        
        if ((op == LOCALREF) || (op == STATICREF)) { 
            /* 
             * if it is a local reference or a static reference we get 
             * back the offset from SB|LB depending on ref type
             */
            offset = pushCount;
            pushCount = 0;
        }      

        OP = LOCALREF;
        stmt.val.visit(this, Integer.valueOf((arg.intValue()) + pushCount));
        if (op == ARRAYUPDATE) {
            Machine.emit(Prim.arrayupd);
        } else if (op == MEMREF) {
            Machine.emit(Prim.fieldupd);
        } else if (op == LOCALREF) {
            if (debug)
                System.out.println("In visitAssignStmt, LOCALREF case");
            Machine.emit(Op.STORE, Reg.LB, offset);
        } else {
            // static ref case
            Machine.emit(Op.STORE, Reg.SB, offset);
        }
        return (Integer.valueOf(VOIDRSIZE));
    }
    
    public Integer visitCallStmt(CallStmt stmt, Integer arg)
    {
        if (debug)
            System.out.println("In call stmt");

        int pushCount = pushArgList(stmt.argList, arg).intValue();
        isMethodCall = true;
        //ignoring the return value from a call statement
        pushCount = stmt.methodRef.visit(this, Integer.valueOf(
                             arg.intValue() + pushCount)).intValue();
        isMethodCall = false;
        if (pushCount > 0)
            Machine.emit(Op.POP, pushCount);

        return (Integer.valueOf(VOIDRSIZE));
    }
    
    public Integer visitIfStmt(IfStmt stmt, Integer arg)
    {
        stmt.cond.visit(this, arg).intValue();
        int patchIf = Machine.nextInstrAddr(); 
        int skipElse = 0;
 
        Machine.emit(Op.JUMPIF, 0, Reg.CB, 0);

        stmt.thenStmt.visit(this, arg).intValue();
        // int skipElse = Machine.nextInstrAddr(); 
        // Machine.emit(Op.JUMP, Reg.CB, 0);

        if (stmt.elseStmt != null) 
        {
            skipElse = Machine.nextInstrAddr();
            Machine.emit(Op.JUMP, Reg.CB, 0);
            int elsePC = Machine.nextInstrAddr();
            Machine.patch(patchIf, elsePC);
            stmt.elseStmt.visit(this, arg);
            Machine.patch(skipElse, Machine.nextInstrAddr());
        } else {
            int endInstr = Machine.nextInstrAddr();
            Machine.patch(patchIf, endInstr);
        }
        return (Integer.valueOf(VOIDRSIZE));
    }
    
    public Integer visitWhileStmt(WhileStmt stmt, Integer arg)
    {
        int J2condEval = Machine.nextInstrAddr();

        Machine.emit(Op.JUMP, Reg.CB, 0);
        int body = Machine.nextInstrAddr();
        stmt.body.visit(this, arg); 
        int condEval  = Machine.nextInstrAddr();
        Machine.patch(J2condEval, condEval);
        stmt.cond.visit(this, arg);
        Machine.emit(Op.JUMPIF, 1, Reg.CB, body); 
        
        return (Integer.valueOf(VOIDRSIZE));
    }
    
    
  // Expressions
    public Integer visitUnaryExpr(UnaryExpr expr, Integer arg)
    {
        if (debug)
            System.out.println("In visitUnaryExpr");
        int pushCount = expr.expr.visit(this, arg);

        visitUnaryOperator(expr.operator, arg);

        return (Integer.valueOf(pushCount));
    }
    
    public Integer visitBinaryExpr(BinaryExpr expr, Integer arg)
    {
        if (debug)
            System.out.println("In visitBinaryExpr");

        int pushCount = expr.left.visit(this, arg).intValue();
        expr.right.visit(this, Integer.valueOf(arg.intValue()+ pushCount));
        visitBinaryOperator(expr.operator, arg);

        return (Integer.valueOf(pushCount));
    }
    
    public Integer visitRefExpr(RefExpr expr, Integer arg)
    {
        return (expr.ref.visit(this, arg));
    }
    
    public Integer visitCallExpr(CallExpr expr, Integer arg)
    {
        if (debug)
            System.out.println("In call expression");

        int pushCount = pushArgList(expr.argList, arg).intValue();
        isMethodCall = true; 
        // need to call the function in the ref method itself
        pushCount = expr.functionRef.visit(this, Integer.valueOf(
                                        arg.intValue() + pushCount)).intValue();
        isMethodCall = false;
        return (Integer.valueOf(pushCount));
    }
    
    public Integer visitLiteralExpr(LiteralExpr expr, Integer arg)
    {
        // will any push the value onto the stack
        return (expr.literal.visit(this, arg));
    
    }
 
    public Integer visitNewArrayExpr(NewArrayExpr expr, Integer arg)
    {
        int pushCount = expr.sizeExpr.visit(this, arg);

        Machine.emit(Prim.newarr); 
        return (Integer.valueOf(ADDRESSSIZE));
    }
    
    public Integer visitNewObjectExpr(NewObjectExpr expr, Integer arg)
    {
        Machine.emit(Op.LOADA, Reg.SB, expr.classtype.classDecl.storage.offset);
        Machine.emit(Op.LOADL, expr.classtype.classDecl.noOfFields);
        Machine.emit(Prim.newobj);
        return (Integer.valueOf(ADDRESSSIZE));
    }

    //Types
    public Integer visitBaseType(BaseType type, Integer arg)
    {
        // can return size based on the type by placing checks
        return (Integer.valueOf(BASETSIZE));
    }

    public Integer visitClassType(ClassType type, Integer arg)
    {
        return (Integer.valueOf(ADDRESSSIZE));
    }

    public Integer visitArrayType(ArrayType type, Integer arg)
    {
        return (Integer.valueOf(ADDRESSSIZE));
    }    
   
    // References
    
    public Integer visitQualifiedRef(QualifiedRef qr, Integer arg)
    {
        if (debug)
            System.out.println("In qualified reference");
        return (Integer.valueOf(VOIDRSIZE));
    }

    public  Integer visitIndexedRef(IndexedRef ir, Integer arg)
    {
        boolean isLHS_backup = false;
        if (debug)
            System.out.println("In Indexed reference");

        int pushCount =  ir.ref.visit(this, arg);

        System.out.println("index reference isLHS: " + isLHS);

        if ((OP == LOCALREF) && isLHS) {
            Machine.emit(Op.LOAD,Reg.LB, pushCount);
            pushCount = 1;
        } else if ((OP == STATICREF) && isLHS) {
            Machine.emit(Op.LOAD,Reg.SB, pushCount);
            pushCount = 1;
        } else if ((OP == MEMREF) && isLHS) {
            Machine.emit(Prim.fieldref);
            pushCount = 1;
        }
    
        // need to backup isLHS flag before we make another reference
        isLHS_backup = isLHS;
        isLHS = false;

        // need to keep track of the pushCount incase if it is a assignment
    	pushCount += ir.indexExpr.visit(this, Integer.valueOf(arg.intValue() + pushCount));

        if (isLHS_backup) {
            OP = ARRAYUPDATE;
            isLHS = true;
            return(Integer.valueOf(pushCount));    
        } 

        Machine.emit(Prim.arrayref);
        return(Integer.valueOf(ADDRESSSIZE));
    }
    
  // Terminals
    public Integer visitIdentifier(Identifier id, Integer arg)
    {
        if (debug)
            System.out.println("In visitIdentifier method");

        return (Integer.valueOf(VOIDRSIZE));
    }
    
    public Integer visitOperator(Operator op, Integer arg)
    {
       return (Integer.valueOf(VOIDRSIZE)); 
    }

    public Integer visitBinaryOperator (Operator op, Integer arg)
    {
        String opName = op.spelling;

        switch (opName) {
            case ("+"):
                Machine.emit(Prim.add);
                break;
            case ("-"):
                Machine.emit(Prim.sub);
                break;
            case ("*"):
                Machine.emit(Prim.mult);
                break;
            case ("/"):
                Machine.emit(Prim.div);
                break;
            case (">="):
                Machine.emit(Prim.ge);
                break;
            case ("<="):
                Machine.emit(Prim.le);
                break;
            case (">"):
                Machine.emit(Prim.gt);
                break;
            case ("<"):
                Machine.emit(Prim.lt);
                break;
            case ("&&"):
                Machine.emit(Prim.and);
                break;
            case ("||"):
                Machine.emit(Prim.or);
                break;
            case ("!="):
                Machine.emit(Prim.ne);
                break;
            case ("=="):
                Machine.emit(Prim.eq);
                break;
            default:
                if (debug)
                    System.out.println("Hit the default case in visit binary operator");
        }
        return (Integer.valueOf(VOIDRSIZE));
    }

    public Integer visitUnaryOperator (Operator op, Integer arg)
    {
        String opName = op.spelling;

        switch (opName) {
            case ("-"):
                Machine.emit(Prim.neg);
                break;
            case ("!"):
                Machine.emit(Prim.not);    
                break;
            default:
                if (debug)
                    System.out.println("Hit the default case in visit Unary operator");
        }
        return (Integer.valueOf(VOIDRSIZE));
    }

    
    public Integer visitIntLiteral(IntLiteral num, Integer arg)
    {
        if (debug)
            System.out.println("In visitIntLiteral");

        Machine.emit(Op.LOADL, Integer.valueOf(num.spelling).intValue());
        return (Integer.valueOf(BASETSIZE));
    }
    
    public Integer visitBooleanLiteral(BooleanLiteral bool, Integer arg)
    {
        int value = 0;
        if (bool.spelling.equals("true"))
            value = 1;
        
        Machine.emit(Op.LOADL, value);
        return (Integer.valueOf(BASETSIZE));
    }

    public Integer visitLocalRef(LocalRef ref, Integer arg) 
    {
        if (debug)
            System.out.println("In local reference");
        if (isLHS) {
            OP = LOCALREF;
            return (Integer.valueOf(ref.decl.storage.offset));
        }

        Machine.emit(Op.LOAD, Reg.LB, ref.decl.storage.offset);
        return (Integer.valueOf(ref.decl.storage.size));
    }

    public Integer visitMemberRef(MemberRef ref, Integer arg)
    {
        if (debug)
            System.out.println("In member reference");

        if (isMethodCall) {
            Machine.emit(Op.LOADA, Reg.OB, 0);
            Machine.emit(Op.CALLD, ref.decl.storage.offset, 0, 0);
            return (Integer.valueOf(ref.decl.storage.size));
        }
        if (isLHS) {
            OP = MEMREF;
            Machine.emit(Op.LOADA, Reg.OB, 0);
            Machine.emit(Op.LOADL, ref.decl.storage.offset);
            return (Integer.valueOf(2));
        }
        Machine.emit(Op.LOAD, Reg.OB, ref.decl.storage.offset);
        return (Integer.valueOf(ref.decl.storage.size));
    }

    public Integer visitClassRef(ClassRef ref, Integer arg)
    {
        if (debug)
            System.out.println("In class reference");

        if (isMethodCall) {
            Machine.emit(Op.LOAD, -1);
            Machine.emit(Op.LOAD, ref.cdecl.storage.offset + 
                                  ref.decl.storage.offset);
            Machine.emit(Op.CALLI);
            return (Integer.valueOf(ref.decl.storage.size));
        }

        if (isLHS) {
            OP = STATICREF;
            return (Integer.valueOf(ref.decl.storage.offset));
        }
        // need to remove this check once static fields are implemented
        if (ref.cdecl.name.equals("System") && ref.decl.name.equals("out")) {
            isPrintCall = true;
            return (Integer.valueOf(0));    
        }
        Machine.emit(Op.LOAD, Reg.SB, ref.decl.storage.offset);
        return (Integer.valueOf(ref.decl.storage.size));
 
    }

    public Integer visitThisRef(ThisRef ref, Integer arg)
    {
        if (debug)
            System.out.println("In this reference"); 
        if (isMethodCall) {
            Machine.emit(Op.LOADA, Reg.OB, 0);
            Machine.emit(Op.CALLD, ref.decl.storage.offset, 0, 0);
            return (Integer.valueOf(ref.decl.storage.size));
        }
        if (isLHS) {
            OP = MEMREF;
            Machine.emit(Op.LOADA, Reg.OB, 0);
            Machine.emit(Op.LOADL, ref.decl.storage.offset);
            return (Integer.valueOf(2));
        }
        if (ref.decl != null) {
            Machine.emit(Op.LOAD, Reg.OB, ref.decl.storage.offset);
        } else {
            Machine.emit(Op.LOADA, Reg.OB, 0);
        }
        if (ref.decl != null)
            return (Integer.valueOf(ref.decl.storage.size));
        else 
            return (Integer.valueOf(ADDRESSSIZE));
    }

    public Integer visitDeRef(DeRef ref, Integer arg)
    {

        if (debug)
            System.out.println("In Dereference" + isMethodCall);

        boolean isMethodCallLocalFlag = isMethodCall;
        boolean isLHSLocalFlag = isLHS;
        // resetting so that further calls do not misinterrupt the meaning of it.
        isMethodCall = false;
        isLHS = false;

        // need to revisit this part after completing recursion code.
        ref.ref.visit(this, arg);
        
        if (isMethodCallLocalFlag) {
            if (isPrintCall && ref.decl.name.equals("println")) {
                Machine.emit(Prim.putint);
                // since we are not checking the return type in identification
                //return (Integer.valueOf(VOIDRSIZE));
            } else {
                Machine.emit(Op.CALLD, ref.decl.storage.offset, 0, 0);
            }
            isMethodCall = isMethodCallLocalFlag;
            //System.out.println("name of the method "+ ref.decl.name + "and its return type is " + ref.decl.storage.size);
            return (Integer.valueOf(ref.decl.storage.size));
        }

        if (isLHSLocalFlag) {
            OP = MEMREF;
            isLHS = isLHSLocalFlag;
            Machine.emit(Op.LOADL, ref.decl.storage.offset);
            return (Integer.valueOf(2));
        }
        Machine.emit(Op.LOADL, ref.decl.storage.offset); 
        Machine.emit(Prim.fieldref);
        isMethodCall = isMethodCallLocalFlag;
        isLHS = isLHSLocalFlag;
        // this is fool proof as the refernced variable can be 
        // a object or a primitive type
        return (Integer.valueOf(ADDRESSSIZE));
    }

    public Integer visitArrayLengthRef(ArrayLengthRef ref, Integer arg)
    {
        int pushCount = ref.ref.visit(this, arg);
        Machine.emit(Op.LOADL, -1);
        Machine.emit(Prim.add);
        Machine.emit(Op.LOADI);
        return (Integer.valueOf(BASETSIZE));
    }


    public Integer pushArgList (ExprList argList, Integer arg)
    {
        int pushCount = 0;

        Expression expr = null;

        for (int i=0; i < argList.size(); i++) {
            expr = argList.get(i);
            pushCount += expr.visit(this, Integer.valueOf(arg.intValue() + pushCount));
        }

        return (Integer.valueOf(pushCount));
    }

    public final int ARRAYUPDATE = 4;
    public final int OBJUPDATE = 3;
    public final int STATICREF = 2;
    public final int MEMREF = 1;
    public final int LOCALREF = 0;
    public int OP = LOCALREF; // default value
    public boolean isLHS = false;
    private int ADDRESSSIZE = 1;
    private int VOIDRSIZE = 0;
    private int BASETSIZE = 1;
}
