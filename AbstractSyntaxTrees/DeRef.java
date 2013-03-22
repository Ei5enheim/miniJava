/*
 * File: DeRef.java
 * Author: Rajesh Gopidi
 * PID:    720367703
 * Course : COMP520
 */

package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class DeRef extends Reference 
{

    
    public DeRef(Reference ref, MemberDecl decl, SourcePosition posn)
    {
        super (posn);
        this.ref = ref;
        this.decl = decl;
    }

    public DeRef(MemberDecl decl, SourcePosition posn)
    {
        super (posn);
        this.ref = null;
        this.decl = decl;
    }

    public DeRef(SourcePosition posn)
    {
        super (posn);
        this.ref = null;
        this.decl = null;
    }
    
    public void setDecl(MemberDecl decl) 
    {
        this.decl = decl;
    }
    
    public void setRef(Reference ref) 
    {
        this.ref = ref;
    }

    public <A,R> R visit(Visitor<A,R> v, A o)
    {
        return v.visitDeRef(this, o);
    }

    public MemberDecl decl ;
    public Reference ref;
}
