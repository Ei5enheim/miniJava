/*
 * File:    Checker.java
 * Author:  Rajesh Gopidi
 * PID:     720367703
 * Course : COMP520
 */
package miniJava.ContextualAnalyzer;

import miniJava.AbstractSyntaxTrees.*;
import miniJava.SyntacticAnalyzer.SourcePosition;
import miniJava.AbstractSyntaxTrees.Package;

public class Checker implements Visitor<Object,Type>
{
    private ErrorReporter reporter; 
    private final ErrorType errorType;
    private final BaseType booleanType;
    private final BaseType intType;
    private final BaseType voidType;
    private final BaseType declType;
    private boolean debug = false, isThisObjRef = false;
    private final SourcePosition pos = new SourcePosition();

    public Checker() 
    {
        reporter = new ErrorReporter();
        errorType = new ErrorType(pos);
        booleanType =  new BaseType(TypeKind.BOOLEAN, pos);
        intType = new BaseType(TypeKind.INT, pos);
        voidType = new BaseType(TypeKind.VOID, pos);
        declType = new BaseType(TypeKind.DECLARATION, pos);
    }

    public void typeCheck (AST ast)
    {
        ast.visit(this, null);
        if (reporter.errorCount > 0)
            System.exit(4);
    }   
    
    // Package
    public Type visitPackage(Package prog, Object arg)
    {
    
        ClassDeclList cl = prog.classDeclList;
        for (ClassDecl c: prog.classDeclList){
            c.visit(this, null);
        }
        return null;
    }
    
    // Declarations
    public Type visitClassDecl(ClassDecl clas, Object arg)
    {
        for (FieldDecl f: clas.fieldDeclList)
            f.type = f.visit(this, null);

        for (MethodDecl m: clas.methodDeclList) {
            m.visit(this, null);
        }
        return voidType;
    } 

    public Type visitFieldDecl(FieldDecl f, Object arg)
    {
        f.type.visit(this, null);
        if (f.type.typeKind == TypeKind.VOID) {
            reporter.reportError("'(' Expected", "", f.posn);
            return errorType;
        } else if (f.type.typeKind == TypeKind.ARRAY){
            if (f.type.visit(this, null).typeKind == TypeKind.VOID) {
                reporter.reportError("'(' Expected", "", f.posn);
                return errorType;
            }
        }
        return (f.type);
    }

    public Type visitMethodDecl(MethodDecl m, Object arg)
    {
        if (debug)
            System.out.println("in method declaration: "+ m.name);
    
        ParameterDeclList pdl = m.parameterDeclList;
        for (ParameterDecl pd: pdl) {
            pd.type = pd.visit(this, null);
        } 
          
        Type type = null;
        StatementList sl = m.statementList;
        for (Statement s: sl) {
            s.visit(this, null);
        }
        if (m.returnExp != null) {
            type = m.returnExp.visit(this, null);
            if (!checkTypes(type, m.type))
            {      
                if (type.typeKind != TypeKind.ERROR)
                    reporter.reportError("Incompatible types - found " +type.toString()+
                                        " but expected " + m.type.toString(), m.name, m.posn);
            }
        } else {
            if (m.type.typeKind != TypeKind.VOID) {
                reporter.reportError("return statement missing in method ", m.name, m.posn);
            }
        }
        return (m.type);
    }
    
    public Type visitParameterDecl(ParameterDecl pd, Object arg) 
    {
        pd.type.visit(this,null); 

        if (debug)
            System.out.println("In parameter declaration check for "+pd.name);

        if (pd.type.typeKind == TypeKind.VOID) {
            reporter.reportError("Illegal start of type", "", pd.posn);
            return errorType;
        } else if (pd.type.typeKind == TypeKind.ARRAY){
            if (pd.type.visit(this, null).typeKind == TypeKind.VOID) {
                reporter.reportError("Illegal start of type", "", pd.posn);
                return errorType;
            }
        }
        return (pd.type);
    } 
    
    public Type visitVarDecl(VarDecl vd, Object arg)
    {
        vd.type.visit(this, null);

        if (vd.type.typeKind == TypeKind.VOID) {
            reporter.reportError("Illegal start of Expression", "", vd.posn);
            vd.type = errorType;
        } else if (vd.type.typeKind == TypeKind.ARRAY){
            if (vd.type.visit(this, null).typeKind == TypeKind.VOID) {
                reporter.reportError("Illegal start of Expression", "", vd.posn);
                vd.type = errorType;
            }
        }
        return (vd.type);
    }
 
    // Statements
    public Type visitBlockStmt(BlockStmt stmt, Object arg)
    {
       
        StatementList sl = stmt.sl;
        for (Statement s: sl) {
             s.visit(this, null);
        }
        return (voidType);
    }
    
    public Type visitVardeclStmt(VarDeclStmt stmt, Object arg)
    {
        Type type1 = null, type2 = null;

        if (stmt.initExp != null)
            type2 = stmt.initExp.visit(this, null);

        type1 = stmt.varDecl.visit(this, null);
        if ((type2 != null) && (!checkTypes(type1, type2)))
            reporter.reportError("aIncompatible types - found "+ type2.toString()+
                                 " but expected "+ type1.toString(), "", stmt.posn);

        return (declType);
    }
    
    public Type visitAssignStmt(AssignStmt stmt, Object arg)
    {
        boolean cond = false;
        Type type1 = null, type2 = null;

        isThisObjRef = false;
        type1 = stmt.ref.visit(this, null);
        if (isThisObjRef) {
            cond = true;
            reporter.reportError("Illegal start of type", "", stmt.posn);
        }

        isThisObjRef = false;
        type2 = stmt.val.visit(this, null);

        if (!cond && !checkTypes(type1, type2))
            reporter.reportError("Incompatible types - found "+ type2.toString()+
                                 " but expected "+ type1.toString(), "", stmt.posn);

        return (voidType);
    }
    
    public  Type visitCallStmt(CallStmt stmt, Object arg)
    {
        // need to check the arguments type in visitIdentifier
        if (debug)
            System.out.println("In call stmt");
        return stmt.methodRef.visit(this, stmt.argList);
    }
    
    public Type visitIfStmt(IfStmt stmt, Object arg)
    {
        Type type1 = null, type2 = null;

        type1 = stmt.cond.visit(this, null);

        if ((type1.typeKind != TypeKind.BOOLEAN) &&
            (type1.typeKind != TypeKind.ERROR)) {
            reporter.reportError("Incompatible types - found "+ type2.toString()+
                                 " but expected "+ booleanType.toString(), "", stmt.cond.posn);
        }
        type2 = stmt.thenStmt.visit(this, null);
        if (type2.typeKind == TypeKind.DECLARATION)
            reporter.reportError("Not a valid Statement ", "", stmt.thenStmt.posn);

        if (stmt.elseStmt != null) 
        {
           type2 =  stmt.elseStmt.visit(this, null);
            if (type2.typeKind == TypeKind.DECLARATION)
                reporter.reportError("Not a valid Statement ", "", stmt.elseStmt.posn);
        }
        return (voidType);
    }
    
    public Type visitWhileStmt(WhileStmt stmt, Object arg)
    {
        Type type1 = null, type2= null;

        type1 = stmt.cond.visit(this, null);
        if ((type1.typeKind != TypeKind.BOOLEAN) &&
            (type1.typeKind != TypeKind.ERROR)) {
            reporter.reportError("Incompatible types - found "+ type2.toString()+
                                 " but expected "+ booleanType.toString(), "", stmt.cond.posn);
        }

        type2 = stmt.body.visit(this, null);
        if (type2.typeKind == TypeKind.DECLARATION)
            reporter.reportError("Not a valid Statement ", "", stmt.body.posn);

        return voidType;
    }
    
    
  // Expressions
    public Type visitUnaryExpr(UnaryExpr expr, Object arg)
    {
        Type type2 = null;

        if (debug)
            System.out.println("In visitUnaryExpr");
        type2 = expr.expr.visit(this, null);
        return (visitUnaryOperator(expr.operator, type2));
    }
    
    public Type visitBinaryExpr(BinaryExpr expr, Object arg)
    {
        Type type2 = null, type3 = null;

        if (debug)
            System.out.println("In visitBinaryExpr");
        type2 = expr.left.visit(this, null);
        type3 = expr.right.visit(this, null);

        return (visitBinaryOperator(expr.operator, type2, type3));
    }
    
    public Type visitRefExpr(RefExpr expr, Object arg)
    {
        Type type = expr.ref.visit(this, null);
        return type;
    }
    
    public Type visitCallExpr(CallExpr expr, Object arg)
    {
        if (debug)
            System.out.println("In call expression");
        Type type  = expr.functionRef.visit(this, expr.argList);
        return type;
    }
    
    public Type visitLiteralExpr(LiteralExpr expr, Object arg)
    {
        Type type = expr.literal.visit(this, null);
        return type;
    }
 
    public Type visitNewArrayExpr(NewArrayExpr expr, Object arg)
    {
        Type type1 = expr.eltType.visit(this, null);
        Type type2 = expr.sizeExpr.visit(this, null);

        if ((type2.typeKind != TypeKind.INT) &&
            (type2.typeKind != TypeKind.ERROR)) {
            reporter.reportError("Incompatible types - found "+ type2.toString()+
                                 " but expected "+ intType.toString(), "",
                                 expr.sizeExpr.posn);
        }

        type1 = new ArrayType(type1, expr.posn);
        return type1;
    }
    
    public Type visitNewObjectExpr(NewObjectExpr expr, Object arg)

    {
        return (expr.classtype);
    }

    //Types
    public Type visitBaseType(BaseType type, Object arg)
    {
        //show(arg, type.typeKind + " " + type.toString());
        return (type);
    }

    public Type visitClassType(ClassType type, Object arg)
    {
        if (type.className.equals("String"))
            type.typeKind = TypeKind.UNSUPPORTED;
        return (type);
    }

    public Type visitArrayType(ArrayType type, Object arg)
    {
        type.eltType.visit(this,null);
        return (type.eltType);
    }    
   
    // References
    
    public Type visitQualifiedRef(QualifiedRef qr, Object arg)
    {
        if (debug)
            System.out.println("In qualified reference");
        return null;
    }

    public Type visitIndexedRef(IndexedRef ir, Object arg)
    {
        boolean cond = false;
        isThisObjRef = false;

        if (debug)
            System.out.println("In Indexed reference");

        Type type1 = ir.ref.visit(this, null);
        if (isThisObjRef) {
            cond = true;
            reporter.reportError("array required, but found "+ 
                                 ((ClassType)type1).className, "", ir.posn);
        }

        if ((type1.typeKind != TypeKind.ARRAY) && !cond) {
            reporter.reportError("array required, but found "+
                                 type1.toString(), "", ir.posn);
            cond = true;
        }

        isThisObjRef = false;
    	Type type2 = ir.indexExpr.visit(this, null);
        if (type2.typeKind != TypeKind.INT) {
            reporter.reportError("Incompatible types - found "+ type2.toString()+
                                 " but expected "+ intType.toString(), "",
                                 ir.indexExpr.posn);
            cond = true;
        }

        if (cond) {
            return (errorType);
        }
        return (((ArrayType)type1).eltType);
    }
    
  // Terminals
    public Type visitIdentifier(Identifier id, Object arg)
    {
        if (debug)
            System.out.println("In visitIdentifier method");

        return (id.decl.visit(this, null));
    }
    
    public Type visitOperator(Operator op, Object arg)
    {
       return voidType; 
    }

    public Type visitBinaryOperator (Operator op, Type type1, Type type2)
    {
        String opName = op.spelling;

        switch (opName) {
            case ("+"):
            case ("-"):
            case ("*"):
            case ("/"):
                if (checkTypes(type1, type2, TypeKind.INT)) {
                    return intType;
                }
                break;
            case (">="):
            case ("<="):
            case (">"):
            case ("<"):
                if (checkTypes(type1, type2, TypeKind.INT)) {
                    return booleanType;
                } 
                break; 
            case ("&&"):
            case ("||"):
                if (checkTypes(type1, type2, TypeKind.BOOLEAN)) {
                    return booleanType;
                } 
                break;
            case ("!="):
            case ("=="):
                if (checkTypes(type1, type2)) {
                    return booleanType;
                } 
                break;
            default:
                if (debug)
                    System.out.println("Hit the default case in visit binary operator");
        }
        reporter.reportError("Operator ' "+opName+" ' cannot be applied to " +
                             type1.typeKind + ", " + type2.typeKind +
                             " types", "", op.posn);
        return errorType;
    }

    public Type visitUnaryOperator (Operator op, Type type1)
    {
        String opName = op.spelling;

        switch (opName) {
            case ("-"):
                if (type1.typeKind == TypeKind.INT ||
                    type1.typeKind == TypeKind.ERROR) {
                    return intType;
                }
                break;
            case ("!"):
                if (type1.typeKind == TypeKind.BOOLEAN ||
                    type1.typeKind == TypeKind.ERROR) {
                    return booleanType;
                }
                break;
            default:
                if (debug)
                    System.out.println("Hit the default case in visit Unary operator");
        }
        reporter.reportError("Operator ' "+opName +" ' cannot be applied to " +
                             type1.typeKind +" type.", "", op.posn);
        return errorType;
    }

    
    public Type visitIntLiteral(IntLiteral num, Object arg)
    {
        return intType;
    }
    
    public Type visitBooleanLiteral(BooleanLiteral bool, Object arg)
    {
        return booleanType;
    }

    boolean isUnsupportedType (Type t)
    {
        if (t.typeKind == TypeKind.UNSUPPORTED) {
            return (true);
        } else if (t.typeKind == TypeKind.ARRAY) {
            if (t.visit(this, null).typeKind == TypeKind.UNSUPPORTED)
                return (true);
        }
        return (false);
    }
    
    public Type visitLocalRef(LocalRef ref, Object arg) 
    {
        Type type = null;
        Reference deRef = ref;

        if (debug)
            System.out.println("In local reference");

        while (deRef.ref != null)
            deRef = deRef.ref;

        if (arg == null) {
            type = deRef.id.decl.type;
            if (isUnsupportedType(type)) {
                reporter.reportError("reference to symbol [" + deRef.id.spelling +
                "] of unsupported type", "", deRef.id.posn);
                return (errorType);
            }
        } else {
            checkArgList((MethodDecl)deRef.id.decl, (ExprList)arg, ref.id.posn);
            type = ((MethodDecl) deRef.id.decl).type;
        }
        return (type);
    }

    public Type visitMemberRef(MemberRef ref, Object arg)
    {

        Type type = null;
        Reference deRef = ref;

        if (debug)
            System.out.println("In member reference");

        while (deRef.ref != null)
            deRef = deRef.ref;

        if (arg == null) {
            type = deRef.id.decl.type;
            if (isUnsupportedType(type)) {
                reporter.reportError("reference to symbol [" + deRef.id.spelling +
                "] of unsupported type", "", deRef.id.posn);
                return (errorType);
            }
        } else {
            checkArgList((MethodDecl)deRef.id.decl, (ExprList)arg, ref.id.posn);
            type = ((MethodDecl) deRef.id.decl).type;
        }
        return (type);
    }

    public Type visitClassRef(ClassRef ref, Object arg)
    {
        Type type = null;
        Reference deRef = ref;

        if (debug)
            System.out.println("In class reference");

        while (deRef.ref != null) {
            deRef = deRef.ref;
        }

        if (arg == null) {
            type = deRef.id.decl.type;
            if (isUnsupportedType(type)) {
                reporter.reportError("reference to symbol [" + deRef.id.spelling +
                "] of unsupported type", "", deRef.id.posn);
                return (errorType);
            }
        } else {
            if (debug)
                System.out.println("checking arguments for the method-"+deRef.id.spelling);
            checkArgList((MethodDecl)deRef.id.decl, (ExprList)arg, ref.id.posn);
            type = ((MethodDecl) deRef.id.decl).type;
        }
        return (type);
    }

    public Type visitThisRef(ThisRef ref, Object arg)
    {
        Type type = null;
        Reference deRef = ref;
        
        if (deRef.ref == null) {
            isThisObjRef = true;
            return (new ClassType(((ClassDecl)deRef.id.decl).name, deRef.id.posn));
        }
        while (deRef.ref != null)
            deRef = deRef.ref;

        if (arg == null) {
            type = deRef.id.decl.type;
            if (isUnsupportedType(type)) {
                reporter.reportError("reference to symbol [" + deRef.id.spelling +
                "] of unsupported type", "", deRef.id.posn);
                return (errorType);
            }
        } else {
            checkArgList((MethodDecl) deRef.id.decl, (ExprList)arg, ref.id.posn);
            type = ((MethodDecl) deRef.id.decl).type;
        }
        return (type);
    }

    public Type visitDeRef(DeRef ref, Object arg)
    {
        return null;
    }

    public boolean checkArgList (MethodDecl decl, ExprList argList, SourcePosition posn)
    {
        Type typea = null;
        boolean errorFound = false;
        ParameterDecl para = null;
        String arglist = "";
        int index = 0;

        if (argList.size() != decl.parameterDeclList.size())
        {
            reporter.reportError("Too many or few arguments specified for method - "+decl.name, "", posn); 
            return false;
        }
        
        Expression expr = null;

        for (int i=0; i < argList.size(); i++) {
            expr = argList.get(i);
            typea = expr.visit(this, null);
            arglist = arglist + typea.toString() + ","; 
            if (typea.typeKind != TypeKind.ERROR) {
                para = decl.parameterDeclList.get(index);
                if (!checkTypes(typea, para.type))
                    errorFound = true;
            } 
            index++;     
        }

        if (errorFound) {
            reporter.reportError("cannot find method  - "+decl.name+"("+
                                 arglist+")", "", posn); 
            return (false);
        }
        return (true);
    }

    public boolean checkTypes (Type type1, Type type2)
    {
        if ((type1.typeKind == TypeKind.ERROR) ||
            (type2.typeKind == TypeKind.ERROR))
            return (true);

        // need to handle unsupported case here

        if ((type1.typeKind != TypeKind.ARRAY) &&
            (type2.typeKind != TypeKind.ARRAY)) {
            if ((type1.typeKind != TypeKind.CLASS) &&
                (type2.typeKind != TypeKind.CLASS)) {
                if ((checkTypes(type1, type2, TypeKind.INT)) ||
                    (checkTypes(type1, type2, TypeKind.BOOLEAN)) ||
                    (checkTypes(type1, type2, TypeKind.VOID))) {
                    return (true);
                }
            } else if ((type1.typeKind == TypeKind.CLASS) &&
                       (type2.typeKind == TypeKind.CLASS)) {
                if (checkClassTypes(type1, type2)) {
                    return (true);
                }
            } else if ((type1.typeKind == TypeKind.UNSUPPORTED) &&
                       (type2.typeKind == TypeKind.UNSUPPORTED)) {
                if (checkClassTypes(type1, type2)) {
                    return (true);
                }
            }
        } else if ((type1.typeKind == TypeKind.ARRAY) &&
                   (type2.typeKind == TypeKind.ARRAY)) {
                    System.out.println("Array match");
            if (checkArrayTypes(type1, type2)) {
                return (true);
            }
        }
        return false;
    }

    public boolean checkClassTypes (Type type1, Type type2)
    {
        ClassType typea = (ClassType) type1;
        ClassType typeb = (ClassType) type2;

        if (typea.className.equals(typeb.className)) {
            System.out.println("returning true for classname "+ typeb.className);
            return true;
        }
        return false;
    }

    public boolean checkArrayTypes (Type type1, Type type2)
    {
        Type typea = type1.visit(this, null);
        Type typeb = type2.visit(this, null);

        if ((typea.typeKind == TypeKind.CLASS) &&
            (typeb.typeKind == TypeKind.CLASS)) {
            System.out.println("class type found in array");
            if (checkClassTypes(typea, typeb)) {
                return true;
            }
        } else if ((typea.typeKind == TypeKind.UNSUPPORTED) &&
                   (typeb.typeKind == TypeKind.UNSUPPORTED)) {
            if (checkClassTypes(typea, typeb)) {
                return (true);
            }
        } else if ((typea.typeKind != TypeKind.CLASS) &&
                   (typeb.typeKind != TypeKind.CLASS)) {
            if ((checkTypes(typea, typeb, TypeKind.INT)) ||
                (checkTypes(typea, typeb, TypeKind.BOOLEAN)) ||
                (checkTypes(typea, typeb, TypeKind.VOID))) {
                return (true);
            }
        }
        return false;
    }

    public boolean checkTypes (Type type1, Type type2, TypeKind rsltType)
    {
        if ((type1.typeKind == rsltType || type1.typeKind == TypeKind.ERROR) &&
            (type2.typeKind == rsltType)|| type2.typeKind == TypeKind.ERROR) {
            return true;
        }
        return false;
    }

}
