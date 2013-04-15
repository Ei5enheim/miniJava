/*
 * File: ArrayLengthRef.java
 * Author: Rajesh Gopidi
 * PID:    720367703
 * Course : COMP520
 */

package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class ArrayLengthRef extends Reference 
{
    public ArrayLengthRef(Reference ref, SourcePosition posn)
    {
        super (posn);
        this.ref = ref;
    }

    public ArrayLengthRef(SourcePosition posn)
    {
        super (posn);
        this.ref = null;
    }
    
    public void ArrayLengthRef(Reference ref) 
    {
        this.ref = ref;
    }

    public <A,R> R visit(Visitor<A,R> v, A o)
    {
        return v.visitArrayLengthRef(this, o);
    }

    public Reference ref;
}
