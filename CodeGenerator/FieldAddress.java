/*
 * File:    FieldAddress.java
 * Author:  Rajesh Gopidi 
 * PID:     720367703
 * Course : COMP520
 */ 

package miniJava.CodeGenerator;

public class FieldAddress extends RuntimeEntity 
{
    public FieldAddress ()
    {
        super();
        offset = 0;
    }

    public FieldAddress (int offset)
    {
        super();
        this.offset = offset;
    }
    
    public FieldAddress (int offset, int size)
    {    
        super(size);
        this.offset = offset;
    }

    public int offset;
}
