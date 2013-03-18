/*
 * File:    ErrorReporter.java
 * Author:  Rajesh Gopidi
 * PID:     720367703
 * Course : COMP520
 */

package miniJava.ContextualAnalyzer;

import java.io.*;
import java.util.*;
import miniJava.SyntacticAnalyzer.SourcePosition;

public class ErrorReporter 
{
    public int errorCount = 0;

    public ErrorReporter ()
    {
        errorCount = 0;
    }
    
    public void reportError (String msg, String tokenID, SourcePosition psn)
    {
        System.out.println("***"+ msg + " " + tokenID + " in line number " + psn.getPosition());
        errorCount++;
    }
}
