package miniJava.SyntacticAnalyzer;

import java.io.*;
import java.util.*;

public class Parser 
{

    Token currentToken;
    Scanner lexicalAnalyzer;
    
    public Parser()
    {
    }

    public Parser(Scanner scanner)
    {
        this.lexicalAnalyzer = scanner;
    }

    public boolean matchID (int tokenID)
    {
        if (currentToken.kind == tokenID)
            return (true);
        else 
            return (false);
    }

    public void acceptTAndLookahead(int tokenID)
    {
        if (matchID(tokenID)) {
            System.out.println("Accepting token type:"+ currentToken.kind);
            currentToken = lexicalAnalyzer.scanToken();
        } else {
            parseError("Expected token" + tokenID + ", but found token"+ currentToken.tokenID);
        }
    }

    public void acceptTAndLookahead()
    {
        System.out.println("Accepting token type:"+ currentToken.kind);
        currentToken = lexicalAnalyzer.scanToken();
    }


    public void parseFile()
    {
        
        currentToken = lexicalAnalyzer.scanToken();

        while (!matchID(Keyword.EOT)) {
            parseClass();
        }
        System.out.println("Successfully parsed the file");
    }

    public void parseClass()
    {
        acceptTAndLookahead(Keywords.CLASS);
        acceptTAndLookahead(Keywords.IDENTIFIER);
        acceptTAndLookahead(Keywords.LCURLY);

        while (!matchID(Keywords.RCURLY)) {
            parseDeclarator();
            parseID();
            if (matchID(Keywords.SEMICOLON))
                acceptTAndLookahead(Keywords.SEMICOLON);
            else 
                parseRestOfMethodDeclaration();
        }

    } 

    public void parseDeclarator()
    {
        switch (currentToken.kind) {
            case (Keywords.PRIVATE):
            case (Keywords.PUBLIC):
                acceptTAndLookahead();
            case (Keywords.STATIC):
                acceptTAndLookahead();
            default:
                parseType();
        }   

    }

    public void parseType()
    {
        switch (currentToken.kind) {
            case (Keywords.INT):
            case (Keywords.IDENTIFIER):
                acceptTAndLookahead();
                if (match(Keywords.LBRACKET)) {
                    acceptTAndLookahead();
                    acceptTAndLookahead(Keywords.RBRACKET);
                }
                break;
            case (Keywords.BOOLEAN):
            case (Keywords.VOID):
                acceptTAndLookahead();
            default:
                parseError("parseType, unexpected token type" + currentToken.tokenID);

        }
    }

    public void parseID()
    {
        acceptTAndLookahead(Keywords.IDENTIFIER);
    }

    public void parseRestOfMethodDeclaration() 
    {
        


    } 

    public void parseError( String str) 
    {
        System.out.println("Parse Error " + str);
        System.exit(4);
    }

}






