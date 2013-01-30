package miniJava.SyntacticAnalyzer;

import java.io.*;
import java.util.*;

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

