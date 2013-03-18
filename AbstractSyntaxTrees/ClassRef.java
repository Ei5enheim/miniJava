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
    public ClassRef (Identifier id, Reference ref, SourcePosition posn) 
    {
        super (id, ref, posn);
        //this.ref = ref;
        //this.id = id;
    }
    
    public ClassRef(Identifier id,  SourcePosition posn)
    {
        super (id, posn);
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
        return null;
        //v.visitClassRef(this, o);
    }

    //public Identifier id;
    //public Reference ref;
}
