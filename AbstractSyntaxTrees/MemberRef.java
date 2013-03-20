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

    public MemberRef(Identifier id,  SourcePosition posn)
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
        return v.visitMemberRef(this, o);
    }

    //public Identifier id;
    //public Reference ref;
}
