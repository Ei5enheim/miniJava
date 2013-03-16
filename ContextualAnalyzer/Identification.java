/*
 * File:    Identifier.java
 * Author:  Rajesh Gopidi
 * PID:     720367703
 * Course : COMP520
 */
package miniJava.ContextualAnalyzer;

import miniJava.AbstractSyntaxTrees.*;
import miniJava.AbstractSyntaxTrees.Package;
public class Identification implements Visitor<String,Object> {
	
    public static boolean showPosition = false;
    private static boolean secondWalk = false;
    private SymbolTable table;
    
    public Identification() 
    {
        // we are going to use the same table for both walks as 
        // everything added in the first walk will be removed
        table = new SymbolTable();
        // add the standard env here and do not forget to increment the level
    }

    public void Identify (AST ast) {
        // need to add the standard environment code here
        ast.visit(this, null);
        secondWalk = true;
        ast.visit(this,null);
    }   
    
    // Package
    public Object visitPackage(Package prog, String arg) {
    
        ClassDeclList cl = prog.classDeclList;
        for (ClassDecl c: prog.classDeclList){
            c.visit(this, null);
        }
        return null;
    }
    
    
  // Declarations
    public Object visitClassDecl(ClassDecl clas, String arg){
       
        // need to add the exit(4) code in the symbol table
        table.add(clas.name, clas);
      
        if (secondWalk) {  
            table.newScope(true); 
            table.add ("this", clas);
            for (FieldDecl f: clas.fieldDeclList)
                f.visit(this, null);
    
            for (MethodDecl m: clas.methodDeclList)
        	m.visit(this, null);
        }
        return null;
    }
    
    public Object visitFieldDecl(FieldDecl f, String arg) {
       
        f.type.visit(this, null); 
        table.add(f.name, f);
        return null;
    }
    
    public Object visitMethodDecl(MethodDecl m, String arg){
       
        table.add(m.name + "function", m);
        table.newScope(true); 
        ParameterDeclList pdl = m.parameterDeclList;
        for (ParameterDecl pd: pdl) {
            pd.visit(this, null);
        }   

        StatementList sl = m.statementList;

        for (Statement s: sl) {
            s.visit(this, null);
        }
        if (m.returnExp != null) {
            m.returnExp.visit(this, null);
        }
        table.closeScope(true);
    
        return null;
    }
    
    public Object visitParameterDecl(ParameterDecl pd, String arg) 
    {
        pd.type.visit(this, null); 
        table.add(pd.name, pd);
        return null;
    } 
    
    public Object visitVarDecl(VarDecl vd, String arg)
    {
        vd.type.visit(this, null);
        table.add(vd.name, vd);
        return null;
    }
 
    // Statements
    public Object visitBlockStmt(BlockStmt stmt, String arg)
    {
        StatementList sl = stmt.sl;
        table.newScope();
        for (Statement s: sl) {
        	s.visit(this, null);
        }
        table.closeScope();
        return null;
    }
    
    public Object visitVardeclStmt(VarDeclStmt stmt, String arg){
        
        if (stmt.initExp != null)
            stmt.initExp.visit(this, null);
        stmt.varDecl.visit(this, null);
        return null;
    }
    
    public Object visitAssignStmt(AssignStmt stmt, String arg){
        stmt.ref = (Reference) stmt.ref.visit(this, null);
        stmt.val.visit(this, null);
        return null;
    }
    
    public Object visitCallStmt(CallStmt stmt, String arg){
        stmt.methodRef = (Reference) stmt.methodRef.visit(this, "true");
        ExprList al = stmt.argList;
        for (Expression e: al) {
            e.visit(this, null);
        }
        return null;
    }
    
    public Object visitIfStmt(IfStmt stmt, String arg){
        stmt.cond.visit(this, null);
        stmt.thenStmt.visit(this, null);
        if (stmt.elseStmt != null)
            stmt.elseStmt.visit(this, null);
        return null;
    }
    
    public Object visitWhileStmt(WhileStmt stmt, String arg){
        stmt.cond.visit(this, null);
        stmt.body.visit(this, null);
        return null;
    }
    
    
  // Expressions
    public Object visitUnaryExpr(UnaryExpr expr, String arg){
        expr.operator.visit(this, null);
        expr.expr.visit(this, null);
        return null;
    }
    
    public Object visitBinaryExpr(BinaryExpr expr, String arg){
        expr.operator.visit(this, null);
        expr.left.visit(this, null);
        expr.right.visit(this, null);
        return null;
    }
    
    public Object visitRefExpr(RefExpr expr, String arg){
        expr.ref = (Reference) expr.ref.visit(this, null);
        return null;
    }
    
    public Object visitCallExpr(CallExpr expr, String arg){
        expr.functionRef = (Reference) expr.functionRef.visit(this, "true");
        ExprList al = expr.argList;
        for (Expression e: al) {
            e.visit(this, null);
        }
        return null;
    }
    
    public Object visitLiteralExpr(LiteralExpr expr, String arg){
        expr.literal.visit(this, null);
        return null;
    }
 
    public Object visitNewArrayExpr(NewArrayExpr expr, String arg){
        expr.eltType.visit(this, null);
        expr.sizeExpr.visit(this, null);
        return null;
    }
    
    public Object visitNewObjectExpr(NewObjectExpr expr, String arg){
        expr.classtype.visit(this, null);
        return null;
    }

    //Types

    public Object visitBaseType(BaseType type, String arg){
        //show(arg, type.typeKind + " " + type.toString());
        return null;
    }

    public Object visitClassType(ClassType type, String arg)
    {
        //show(arg, type);
        //showQuoted(indent(arg), type.className);
        if (table.retrieve(type.className) == null) {
            //report error
        }
        return null;
    }

    public Object visitArrayType(ArrayType type, String arg){
        //show(arg, type);
        type.eltType.visit(this, null);
        return null;
    }    
   
  // References
    
    public Object visitQualifiedRef(QualifiedRef qr, String isMCall)
    {
        Reference ref = null, ret = null;
        Declaration decl = null;        
        int level = -1;
        Identifier id = null;
        String name = null;
        boolean isMethodCall = false;

        if ((isMCall != null) && isMCall.equals("true"))
            isMethodCall = true;

        if (qr.thisRelative) {
            if ((decl = table.retrieve("this")) != null) {
                id = new Identifier("this", qr.posn);
                ref = new ThisRef(id, qr.posn);
                ret = ref;
                id = null;
            } else {
                // report error.
            }
        }

        IdentifierList ql = qr.qualifierList;
        if (ql.size() > 0) {
            id = ql.get(0);
            if (isMethodCall && ql.size() == 1) {
                name = id.spelling + "function";
            } else {
                name = id.spelling;
            }

            if ((decl = table.retrieve(name)) != null) {
                id.decl = decl;
            } else {
                //report error
            }

            if (ref == null) {
                level = table.retrieveLevel(name);
                if (level > 2) {
                    ref = new LocalRef(id, qr.posn);
                } else if (level == 2) {
                    ref = new MemberRef(id, qr.posn);
                } else if (level == 1) {
                    ref = new ClassRef(id, qr.posn);
                } else {
                    // report error    
                }
                ret = ref;
            } else {
                // this ref case
                ref.ref = new DeRef(id, id.posn);
                ref = ref.ref; 
            }      
        }

        for (int i = 1; i < ql.size(); i++) {
            id = ql.get(i);
            name = id.spelling;
            if (isMethodCall && (i  == (ql.size() - 1))) {
                decl = retrieveMethodDeclaration(decl, name);
            } else {
                decl = retrieveMethodDeclaration(decl, name);
            }
            if (decl != null) {
                id.decl = decl;
                ref.ref = new DeRef(id, id.posn);
                ref = ref.ref;
            } else {
                //report error;
            }    
        }
        return ret;
    }

    // Retrieving the declaration

    public Declaration retrieveFieldDeclaration (Declaration decl, String key)
    {
        ClassDecl clas = (ClassDecl) decl;
        boolean found = false; 
        
        for (FieldDecl f: clas.fieldDeclList) {
            if (key.equals(f.name)) {
                return (f);
            }
        }
        return null;    
    }

    public Declaration retrieveMethodDeclaration (Declaration decl, String key)
    {
        ClassDecl clas = (ClassDecl) decl;

        for (MethodDecl m: clas.methodDeclList) {
            if (key.equals(m.name)) {
                return (m);
            }
        }
        return null;
    }
    
    public Object visitIndexedRef(IndexedRef ir, String arg) {
    	//show(arg, ir);
        ir.ref = (Reference) ir.ref.visit(this, null);
    	ir.indexExpr.visit(this, null);
    	return ir;
    }
    
  // Terminals
    public Object visitIdentifier(Identifier id, String arg){
        //show(arg, "\"" + id.spelling + "\" " + id.toString());
        return null;
    }
    
    public Object visitOperator(Operator op, String arg){
        //show(arg, "\"" + op.spelling + "\" " + op.toString());
        return null;
    }
    
    public Object visitIntLiteral(IntLiteral num, String arg){
        //show(arg, "\"" + num.spelling + "\" " + num.toString());
        return null;
    }
    
    public Object visitBooleanLiteral(BooleanLiteral bool, String arg){
        //show(arg, "\"" + bool.spelling + "\" " + bool.toString());
        return null;
    }

    /**
    public Object visitLocalRef(LocalRef ref, String arg) 
    {
        return null;
    }

    public Object visitMemberRef(MemberRef ref, String arg)
    {
        return null;
    }

    public Object visitClassRef(ClassRef ref, String arg)
    {
        return null;
    }

    public Object visitThisRef(ThisRef ref, String arg)
    {
        return null;
    }

    public Object visitDeRef(DeRef ref, String arg)
    {
        return null;
    }
    */
    public void reportError(String str) 
    {
        // add the code here 
        System.exit(4);
    }

    
}
