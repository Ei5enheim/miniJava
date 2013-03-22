/*
 * File:    Identification.java
 * Author:  Rajesh Gopidi
 * PID:     720367703
 * Course : COMP520
 */
package miniJava.ContextualAnalyzer;

import miniJava.AbstractSyntaxTrees.*;
import miniJava.SyntacticAnalyzer.SourcePosition;
import miniJava.AbstractSyntaxTrees.Package;

public class Identification implements Visitor<String,Object> {
	
    public boolean showPosition = false;
    private boolean secondWalk = false, debug = false;
    private SymbolTable table;
    private boolean isStaticMethod = false, mainMethodFound = false;
    private String currentClass = null;
    private ErrorReporter reporter;
    
    public Identification() 
    {
        // we are going to use the same table for both walks as 
        // everything added in the first walk will be removed
        table = new SymbolTable();
        reporter = new ErrorReporter();
        // add the standard env here and do not forget to increment the level
        table.newScope();
	table.newScope();
        addStandardImports();
    }

    public void Identify (AST ast)
    {
        // need to add the standard environment code here
        ast.visit(this, null);
        secondWalk = true;
        ast.visit(this,null);
        // done with this check
        if (!mainMethodFound) {
            reporter.reportError("No definition of required main found in the program", "", ast.posn);
        }
        if (reporter.errorCount > 0)
            System.exit(4);
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
        if (debug)
            System.out.println(" In class decl " + clas.name);
        if (!secondWalk) {
            if (table.add(clas.name, clas)) {
                reporter.reportError("class " + clas.name+" is already defined",
                                     "", clas.posn);        
            }
        }

        if (secondWalk) {  
            // parent scope is being set for the case where we have a block inside in the class
            table.newScope(true); 
            
            for (FieldDecl f: clas.fieldDeclList)
                f.visit(this, null);
    
            for (MethodDecl m: clas.methodDeclList) {
                if ((table.retrieveMemberDecl(m.name) != null) ||
                    (table.retrieveMemberDecl(m.name+"function") != null)) {
                    reporter.reportError(m.name+" is already defined in the class", "", m.posn);
                }  else if (table.add(m.name+"function", m)) {
                    reporter.reportError("Method" +m.name+" is already defined in the class", "", m.posn);
                }
            }
            currentClass = clas.name;
            for (MethodDecl m: clas.methodDeclList) {
                isStaticMethod = false;
        	m.visit(this, null);
            }
            table.closeScope(true);
        }
        return null;
    }
    /**
    boolean isBasetype (Type t)
    {
        if ((t.typeKind == TypeKind.VOID) ||
            (t.typeKind == TypeKind.INT) ||
            (t.typeKind == TypeKind.BOOLEAN)) {
            return (true);
        }
        return (false);
    } 

    boolean isValidClasstype (Type t)
    {
        if (t.typeKind == TypeKind.CLASS) {
            if ((retrieveClassDecl(t.className) != null)
                return (true);
        }
        return (false);
    }

    boolean isValidArraytype (Type t)
    {
        if (t.typeKind == TypeKind.ARRAY) {
            if (isBasetype(t.eltType) {
                return (true);
            } else if (isValidClasstype(t.eltType)) {
                return (true);
            }
        }
        return (false);
    }
    */
    
    public Object visitFieldDecl(FieldDecl f, String arg)
    {
       
        f.type.visit(this, null); 
        if (table.add(f.name, f)) {
            reporter.reportError(f.name + " is already defined in class ", "", f.posn);
        }
        return null;
    }

    public void visitMainMethodDecl(MethodDecl m)
    {
        ParameterDeclList pdl = m.parameterDeclList;
        ParameterDecl pd = null;

        if (!m.isPublic) {
            return;
        }

        if (!m.isStatic) {
            return;
        }

        if (m.type.typeKind != TypeKind.VOID) {
	    return;
        }
        
        if (pdl.size() > 0)
            pd = pdl.get(0);

        if ((pdl.size() > 1) || (pdl.size() < 1)) {
            return;
        } else if (pd.type.typeKind != TypeKind.ARRAY) {
            return;
        } else if (((ArrayType) pd.type).eltType.typeKind != TypeKind.CLASS) {
            return;
        } else if (!(((ClassType)((ArrayType) pd.type).eltType).className.equals("String"))) {
            return;
        }
	
        if (mainMethodFound) {
            reporter.reportError("More than one public static void main (String[] args) method found in the program",
				 "", m.posn);
        }
	mainMethodFound = true;
        
    }
    
    public Object visitMethodDecl(MethodDecl m, String arg)
    {
        table.newScope(true); 
        isStaticMethod = m.isStatic;

        if (m.name.equals("main")) {
           visitMainMethodDecl(m);
        } 
        // checking for return type;       
        m.type.visit(this,null); 
        
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
        if (table.add(pd.name, pd))  {
            reporter.reportError("Variable " +pd.name+" is already defined","", pd.posn);
        }
        return null;
    } 
    
    public Object visitVarDecl(VarDecl vd, String arg)
    {
        vd.type.visit(this, null);
        if (table.add(vd.name, vd)) {
            reporter.reportError(vd.name+" is already defined in the method ", 
                                 "", vd.posn);
        }
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
        stmt.ref = (Reference) stmt.ref.visit(this, "false");
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
        expr.ref = (Reference) expr.ref.visit(this, "false");
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
        Declaration decl = null;
        if ((decl = table.retrieveClassDecl(type.className)) == null) {
            reporter.reportError("Cannot find symbol", type.className, type.posn);
        }
        type.classDecl = (ClassDecl) decl;
        return decl;
    }

    public Object visitArrayType(ArrayType type, String arg){
        return (type.eltType.visit(this, null));
    }    
   
  // References
    
    public Object visitQualifiedRef(QualifiedRef qr, String isMCall)
    {
        Reference ref = null;
        Declaration decl = null, classDecl = null;        
        int level = -1;
        Identifier id = null;
        String name = null, className = null;
        boolean isMethodCall = false, isStatic = false, isPrivate = false;
        boolean isLocalVar = false;
        IdentifierList ql = qr.qualifierList;
    
        if ((isMCall != null) && isMCall.equals("true"))
            isMethodCall = true;

        if (qr.thisRelative && isStaticMethod) {
            reporter.reportError("this cannot be referenced " + 
                                 "from a static method", " ", qr.posn);
            return (qr);
        } else if (qr.thisRelative){
	    if (isMethodCall && ql.size() < 1) {
		reporter.reportError("cannot find symbol - method-", "this", qr.posn);
                return (qr);
	    }
	    decl = table.retrieve(currentClass);
            ref = new ThisRef((ClassDecl) decl, qr.posn);
        }

        if (ql.size() > 0) {
            id = ql.get(0);
            if (isMethodCall && ql.size() == 1) {
                if (debug)
                    System.out.println("doing method search");
                name = id.spelling + "function";
            } else {
                name = id.spelling;
            }
           
            if ((decl = table.retrieve(name)) == null) {
                reporter.reportError("cannot find symbol -", id.spelling, id.posn);
                return (qr);
            }
	    level = table.retrieveLevel(name);
            if  (debug) {
                System.out.println("level = " + level + " of id = "+ id.spelling );
            }
            if (ref == null) {   
                if (level > 2) {
                    ref = new LocalRef((LocalDecl) decl, qr.posn);
                    classDecl = retrieveClassDecl(decl);
                    isLocalVar = true;
                } else if (level == 2) {
                    if (isStaticMethod && !(((MemberDecl)decl).isStatic)) {
                        reporter.reportError("non static variable ' " + id.spelling + 
                                             " ' cannot be referenced from a static context ", 
                                             " ", id.posn);
                        return (qr);
                    } 
                    ref = new MemberRef((MemberDecl) decl, qr.posn);
                    classDecl = retrieveClassDecl(decl);
                } else if ((level == 1) && (ql.size() != 1)) {
                    ref = new ClassRef((ClassDecl) decl, qr.posn);
                    classDecl = decl;
                } else {
                    reporter.reportError("cannot find symbol - variable ", id.spelling, id.posn);
                    return (qr);
                }
            } else {
                if ((decl = table.retrieveMemberDecl(name)) == null) {
                    reporter.reportError("cannot find symbol -", id.spelling, id.posn);
                    return (qr);
                }
                ((ThisRef)ref).setMemberDecl((MemberDecl)decl);
                classDecl = retrieveClassDecl(decl);
            }      
        }

        for (int i = 1; i < ql.size(); i++) {
            isLocalVar = false;
            id = ql.get(i);
            if (classDecl == null) {
                reporter.reportError("unable to derefence variable-", id.spelling, id.posn);
                return (qr);
            }
            name = id.spelling;
            className = classDecl.name;
            // Method name is passed as an argument
            if (isMethodCall && (i  == (ql.size() - 1))) {
                decl = retrieveMethodDeclaration(classDecl, name);
                if (debug)
                    System.out.println("Found method declaration for "+name);
            } else {
                decl = retrieveFieldDeclaration(classDecl, name);
                if (debug)
                    System.out.println("Found field declaration for "+name);
            }

            if (decl != null) {
                isStatic = ((MemberDecl) decl).isStatic;
                isPrivate = ((MemberDecl) decl).isPrivate;

                if (isPrivate && !className.equals(currentClass)) {
                    reporter.reportError(id.spelling + " has private access in ", className, id.posn);
                    return (qr);
                }
                if ((level == 1) && (i == 1) && !isStatic) {
                    reporter.reportError("non static variable ' " + id.spelling +
                            "' :cannot be referenced from a static context ",
                            " ", id.posn);
                    return (qr);
                } 
                ref = new DeRef(ref, (MemberDecl)decl, id.posn);
                classDecl = retrieveClassDecl(decl);
            } else {
                reporter.reportError("cannot find symbol - variable-", id.spelling, id.posn);
                return (qr);
            }    
        }

	//  check to prevent static access and referencing class, ignoring if "this" is being referenced
        if (!isLocalVar && !(qr.thisRelative && (ql.size() < 1)) && ((MemberDecl) decl).isStatic) {
            reporter.reportError("PA3 no static access error", id.spelling, id.posn);
            // report error "PA3 no static access error"
        }
        return ref;
    }

    public ClassDecl retrieveClassDecl (Declaration decl)
    {
        return ((ClassDecl)decl.type.visit(this, null));
    }


    // Retrieving the declaration

    public Declaration retrieveFieldDeclaration (Declaration decl, String key)
    {
        ClassDecl clas = (ClassDecl) decl;
        
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

    public ClassDecl addStringClassDef()
    {
        ClassDecl classDecl = null;
        FieldDeclList fieldDeclList = new FieldDeclList();
        MethodDeclList methodDeclList = new MethodDeclList();
        SourcePosition pos = new SourcePosition();
        classDecl = new ClassDecl("String", fieldDeclList, methodDeclList, pos);
        return classDecl;
    }

    public ClassDecl addPrintStreamClassDef()
    {
        ParameterDecl paraDecl = null;
        ClassDecl classDecl = null;
        FieldDeclList fieldDeclList = new FieldDeclList();
        MethodDeclList methodDeclList = new MethodDeclList();
        SourcePosition pos = new SourcePosition();
    
        Type typ = new BaseType(TypeKind.VOID, pos);
        Type typarg = new BaseType(TypeKind.INT, pos);
        FieldDecl fieldDecl = new FieldDecl(false, false, typ, "println", pos);
        fieldDecl.isPublic = true;
        paraDecl = new ParameterDecl(typarg, "n", pos);
        ParameterDeclList paraList = new ParameterDeclList();
        paraList.add(paraDecl);
        MethodDecl decl = new MethodDecl(fieldDecl, paraList, new StatementList(), null, pos);
        methodDeclList.add(decl);
        classDecl = new ClassDecl("_PrintStream", fieldDeclList, methodDeclList, pos);
        return classDecl;
    }

    public ClassDecl addSystemClassDef(ClassDecl printStreamClass)
    {
        ClassDecl classDecl = null;
        FieldDeclList fieldDeclList = new FieldDeclList();
        MethodDeclList methodDeclList = new MethodDeclList();
        SourcePosition pos = new SourcePosition();
        ClassType typ = new ClassType("_PrintStream", pos);
        typ.classDecl = printStreamClass;
        FieldDecl fieldDecl = new FieldDecl(false, true, typ, "out", pos);
        fieldDecl.isPublic = true;
        fieldDeclList.add(fieldDecl);
        classDecl = new ClassDecl("System", fieldDeclList, methodDeclList, pos);
        return classDecl;
    }

    public void addStandardImports()
    {
        ClassDecl printStreamClass = addPrintStreamClassDef();
        table.add("_PrintStream", printStreamClass);
        
        ClassDecl systemClass = addSystemClassDef(printStreamClass);
        table.add("System", systemClass);
        ClassDecl stringClass = addStringClassDef();
        table.add("String", stringClass);

    }
    
}
