/*
 * File: MemberRef.java
 * Author: Rajesh Gopidi
 * PID:    720367703
 * Course : COMP520
 */

package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class MemberRef extends Reference 
{

    public MemberRef(MemberDecl decl,  SourcePosition posn)
    {
        super(posn);
        this.decl = decl;
    }

    public MemberRef(SourcePosition posn)
    {
        super(posn);
        this.decl = null;
    }
    
    public void setDecl(MemberDecl decl) 
    {
        this.decl = decl;
    }

    public <A,R> R visit(Visitor<A,R> v, A o)
    {
        return v.visitMemberRef(this, o);
    }

    public MemberDecl decl;
}
