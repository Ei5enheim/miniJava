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

    public LocalRef(Identifier id,  SourcePosition posn)
    {
        super(id, posn);
        //this.ref = null;
        //this.id = id;
    }
    
    /*
    public void setRef(Reference ref) 
    {
        this.ref = ref;
    }*/
	
    public <A,R> R visit(Visitor<A,R> v, A o)
    {
        return v.visitLocalRef(this, o);
    }

    //public Identifier id;
    //public Reference ref;
}