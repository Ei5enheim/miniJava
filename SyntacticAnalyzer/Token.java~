/*
 * File: Token.java
 * Author: Rajesh Gopidi
 * PID:    720367703
 * Course : COMP520
 */

package miniJava.SyntacticAnalyzer;

import java.io.*;
import java.util.*;
import miniJava.SyntacticAnalyzer.*;
/*
 * Class Token
 *
 * Objects of this class hold the attributes of a token
 * such as token type [keyword, id, num etc], ID [spelling of
 * the token] and position of the token.
 *
 */
public final class Token 
{

    private int kind;
    private String tokenID;
    private SourcePosition pos;

    public Token()
    {
    
    }

    public Token(int kind, String tokenID, SourcePosition pos) 
    {
        this.kind = kind;
        this.tokenID = tokenID;
        this.pos = pos;
    }
    
    public String toString ()
    {
        return (kind + " " + tokenID + " " + pos.getPosition());
    }
    public int getKind() 
    {
        return (kind);
    }
    
    public String getTokenID()
    {
        return (tokenID);
    }
}

