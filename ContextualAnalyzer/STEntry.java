/*
 * File:    STEntry.java
 * Author:  Rajesh Gopidi
 * PID:     720367703
 * Course : COMP520
 */

package miniJava.ContextualAnalyzer;

import miniJava.AbstractSyntaxTrees.Declaration;

public class STEntry
{
    protected Declaration astNode;
    protected STEntry next; 
   
    public STEntry (Declaration decl, STEntry next)
    {

        this.astNode = decl;
        this.next  = next;
    }

}
