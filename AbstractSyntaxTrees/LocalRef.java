/*
 * File: LocalRef.java
 * Author: Rajesh Gopidi
 * PID:    720367703
 * Course : COMP520
 */

package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class LocalRef extends Reference 
{

    public LocalRef(LocalDecl decl,  SourcePosition posn)
    {
        super(posn);
        this.decl = decl;
    }

    public LocalRef(SourcePosition posn)
    {
        super(posn);
        this.decl = null;
    }
 
    
    public void setDecl(LocalDecl decl) 
    {
        this.decl = decl;
    }
	
    public <A,R> R visit(Visitor<A,R> v, A o)
    {
        return v.visitLocalRef(this, o);
    }

    public LocalDecl decl;

}
