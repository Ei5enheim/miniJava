/*
 * File: ThisRef.java
 * Author: Rajesh Gopidi
 * PID:    720367703
 * Course : COMP520
 */

package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class ClassRef extends Reference 
{
    
    public ClassRef(ClassDecl decl, SourcePosition posn)
    {
        super (posn);
        this.decl = decl;
    }

    public ClassRef(SourcePosition posn)
    {
        super (posn);
        this.decl = null;
    }
    
    public void setDecl(ClassDecl decl) 
    {
        this.decl = decl;
    }

    public <A,R> R visit(Visitor<A,R> v, A o)
    {
        return (v.visitClassRef(this, o));
    }
    
    public ClassDecl decl;
}
