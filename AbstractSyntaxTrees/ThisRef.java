/*
 * File: ThisRef.java
 * Author: Rajesh Gopidi
 * PID:    720367703
 * Course : COMP520
 */

package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class ThisRef extends Reference 
{

    public ThisRef (SourcePosition posn) 
    {
        super(posn);
    }
    
    public ThisRef (Identifier id,  SourcePosition posn)
    {
        super(id, posn);
        //this.ref = null;
        //this.id = id;
    }
    
    /**
    public void setRef(Reference ref) 
    {
        this.ref = ref;
    }*/

    public <A,R> R visit(Visitor<A,R> v, A o)
    {
        return v.visitThisRef(this, o);
    }

    //public Identifier id;
    //public Reference ref;
}
