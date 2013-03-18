/*
 * File: UnsupportedType.java
 * Author: Rajesh Gopidi
 * PID:    720367703
 * Course : COMP520
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class UnsupportedType extends Type
{
    public UnsupportedType(String cn, SourcePosition posn){
        super(TypeKind.UNSUPPORTED, posn);
        className = cn;
        classDecl = null;
    }

    public UnsupportedType(SourcePosition posn){
	super(TypeKind.UNSUPPORTED, posn);
    }
  
    public UnsupportedType (ClassType ct) 
    {
	super(TypeKind.UNSUPPORTED, ct.posn);
	className = ct.className;
	classDecl = ct.classDecl;
    }

    public <A,R> R visit(Visitor<A,R> v, A o) {
        return v.visitUnsupportedType(this, o);
    }

    public String className;
    public ClassDecl classDecl;  // resolved in contextual analysis
}
