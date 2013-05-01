/*
 * File: ThisRef.java
 * Author: Rajesh Gopidi
 * PID:    720367703
 * Course : COMP520
 */

package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class StaticRef extends Reference 
{
    
    public StaticRef(ClassDecl decl, SourcePosition posn)
    {
        super (posn);
        this.cdecl = decl;
	this.decl = null;
    }

    public StaticRef(ClassDecl cdecl, MemberDecl decl, SourcePosition posn)
    {
        super (posn);
        this.cdecl = cdecl;
        this.decl = decl;
    }

    public StaticRef(SourcePosition posn)
    {
        super (posn);
        this.cdecl = null;
	this.decl = null;
    }

    public StaticRef(MemberDecl decl, SourcePosition posn)
    {
        super (posn);
        this.cdecl = null;
        this.decl = decl;
    }

    
    public void setCDecl(ClassDecl decl) 
    {
        this.cdecl = decl;
    }

    public void setDecl(MemberDecl decl)
    {

	this.decl = decl;
    }

    public <A,R> R visit(Visitor<A,R> v, A o)
    {
        return (v.visitStaticRef(this, o));
    }
    
    public MemberDecl decl;
    public ClassDecl cdecl;
    
}
