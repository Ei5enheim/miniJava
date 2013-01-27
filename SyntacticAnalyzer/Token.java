package miniJava.SyntacticAnalyzer;

import java.io.*;
import java.util.*;

public final class Token 
{

    private int kind;
    private String tokenId;
    private SourcePosition pos;

    public Token()
    {
    
    }

    public Token(int kind, String tokenID, SourcePosition pos) 
    {
        this.kind = kind;
        this.tokenId = tokenID;
        this.pos = pos;
    }
    
    public String toString ()
    {
        return (kind + " " + tokenId + " " + pos.getPosition());
    }
    public int getKind() 
    {
        return (kind);
    }
}

