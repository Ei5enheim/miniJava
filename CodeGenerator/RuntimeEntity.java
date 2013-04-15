/*
 * File:    RuntimeEntity.java
 * Author:  Rajesh Gopidi
 * PID:     720367703
 * Course : COMP520
 */

package miniJava.CodeGenerator;


public abstract class RuntimeEntity
{

    public RuntimeEntity ()
    {
        size = 1;
    }

    public RuntimeEntity (int size)
    {
        this.size = size;
    }

    public int size;
}
