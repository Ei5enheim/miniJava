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

    public boolean match (int tokenID)
    {
        if (currentToken.getKind() == tokenID)
            return (true);
        else 
            return (false);
    }

    public void acceptTAndLookahead(int tokenID)
    {
        if (match(tokenID)) {
            System.out.println("Accepting token type:"+ currentToken.getTokenID());
            currentToken = lexicalAnalyzer.scanToken();
        } else {
            parseError("Expected token" + currentToken.getTokenID() + ", but found token"+ Keywords.tokenTable[tokenID]);
        }
    }

    public void acceptTAndLookahead()
    {
        System.out.println("Accepting token type:"+ currentToken.getTokenID());
        currentToken = lexicalAnalyzer.scanToken();
    }

    public void parseFile()
    {
        
        currentToken = lexicalAnalyzer.scanToken();

        while (!match(Keywords.EOT)) {
            parseClass();
        }
        System.out.println("Successfully parsed the file");
    }

    public void parseClass()
    {
        acceptTAndLookahead(Keywords.CLASS);
        acceptTAndLookahead(Keywords.IDENTIFIER);
        acceptTAndLookahead(Keywords.LCURLY);

        while (!match(Keywords.RCURLY)) {
            parseDeclarator();
            parseID();
            if (match(Keywords.SEMICOLON))
                acceptTAndLookahead(Keywords.SEMICOLON);
            else 
                parseRestOfMethodDeclaration();
        }

    } 

    public void parseDeclarator()
    {
        switch (currentToken.getKind()) {
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
        switch (currentToken.getKind()) {
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
                parseError("parseType, unexpected token type" + currentToken.getTokenID());

        }
    }

    public void parseID()
    {
        acceptTAndLookahead(Keywords.IDENTIFIER);
    }

    public void parseRestOfMethodDeclaration() 
    {
        acceptTAndLookahead(Keywords.LPAREN);
        if (!match(Keywords.RPAREN)) {
            while (true) {
                parseType();
                acceptTAndLookahead(Keywords.IDENTIFIER);
                if (match(Keywords.COMMA))
                    acceptTAndLookahead();
                else 
                    break;
            }
        }
        acceptTAndLookahead(Keywords.RPAREN);
        acceptTAndLookahead(Keywords.LCURLY);
        while (!match(Keywords.RCURLY)) {
            parseStmt();
            if (match(Keywords.RETURN)) {
                acceptTAndLookahead();
                parseExpression();
                acceptTAndLookahead(Keywords.SEMICOLON);
            }   
        }
        acceptTAndLookahead();
    } 

    public void parseStmt() 
    {
        int kind = currentToken.getKind();
        acceptTAndLookahead();
        switch (kind) {
            case (Keywords.LCURLY):
                while (!match(Keywords.RCURLY)) {
                    parseStmt();
                }
                acceptTAndLookahead();
                break;
            case (Keywords.THIS):
                parseReference();
                break;
            case (Keywords.INT):
                parseBrackets(false);
                parseRestOfAssignmentStmt(); 
                break;
            case (Keywords.BOOLEAN):
            case (Keywords.VOID):
                acceptTAndLookahead(Keywords.IDENTIFIER);
                parseRestOfAssignmentStmt(); 
                break;
            case (Keywords.IDENTIFIER):
                parseBrackets(false);
                if (match(Keywords.IDENTIFIER)) {
                    parseRestOfAssignmentStmt();
                } else {
                    parseReference();
                }
                break;
            case (Keywords.IF):
                parseIFStatement();
                break;
            case (Keywords.WHILE):
                acceptTAndLookahead(Keywords.LPAREN);
                parseExpression();
                acceptTAndLookahead(Keywords.RPAREN);
                parseStmt();
            default:
                parseError("parseType, unexpected token type" + currentToken.getTokenID());
        }

    }

    public void parseIFStatement()
    {
        acceptTAndLookahead(Keywords.LPAREN);
        parseExpression();
        acceptTAndLookahead(Keywords.RPAREN);
        parseStmt();
        if (match(Keywords.ELSE)) {
            acceptTAndLookahead();
            parseStmt();
        }
    }

    public void parseReference()
    {
        if (match(Keywords.DOT)) {
            do {
                acceptTAndLookahead();
                acceptTAndLookahead(Keywords.IDENTIFIER);
            } while (match(Keywords.DOT));
        }
        if (match(Keywords.LPAREN)) {
            acceptTAndLookahead();
            while (!match(Keywords.RPAREN)) {
                parseArgumentList();     
            }
            acceptTAndLookahead();
            acceptTAndLookahead(Keywords.SEMICOLON);
        } else {
            if (match(Keywords.LBRACKET)) {
                acceptTAndLookahead();
                parseExpression();
                acceptTAndLookahead(Keywords.RBRACKET);
            }
            acceptTAndLookahead(Keywords.BECOMES);
            parseExpression();
            acceptTAndLookahead(Keywords.SEMICOLON); 
        }
    }

    public void parseArgumentList()
    {
        parseExpression();
        if (match(Keywords.COMMA)) {
            do {
                acceptTAndLookahead();
                parseExpression();
            } while (match(Keywords.COMMA));
        }
    }

    public void parseRestOfAssignmentStmt()
    {
        acceptTAndLookahead(Keywords.BECOMES);
        parseExpression();
        acceptTAndLookahead(Keywords.SEMICOLON);
    }

    public void parseBrackets(boolean exitOnError)
    {
        if (match(Keywords.LBRACKET)) {
            acceptTAndLookahead();
            if (match(Keywords.RBRACKET))
                acceptTAndLookahead();
            else
                parseError("Expecting Unxpectedtokentype:" + currentToken.getTokenID() + " expecting token:"+ Keywords.tokenTable[Keywords.RBRACKET]);  
        }
    }

    public void parseExpression()
    {
        int kind;
        while (true) {
            kind = currentToken.getKind();
            acceptTAndLookahead();
            switch (kind) {
            case (Keywords.THIS):
            case (Keywords.IDENTIFIER):
                parseReference();
                break;
            case (Keywords.LPAREN):
                parseExpression();
                break;
            case (Keywords.NUMBER):
            case (Keywords.TRUE):
            case (Keywords.FALSE):
                break;
            case (Keywords.NEW):
                acceptTAndLookahead(Keywords.LPAREN);
                if (match(Keywords.IDENTIFIER)) {
                    acceptTAndLookahead();
                    if (match(Keywords.LPAREN)) {
                        acceptTAndLookahead();
                        acceptTAndLookahead(Keywords.RPAREN);
                    } else if (match(Keywords.LBRACKET)) {
                        parseExpression();
                        acceptTAndLookahead(Keywords.RBRACKET);
                    } else {
                        parseError("unexpected token:"+ currentToken.getTokenID());
                    }
                } else if (match(Keywords.INT)) {
                    acceptTAndLookahead();
                    if (match(Keywords.LBRACKET)) {
                        parseExpression();
                        acceptTAndLookahead(Keywords.RBRACKET);
                    } else {
                        parseError("unexpected token:"+ currentToken.getTokenID());
                    }
                } else { 
                    parseError("unexpected token:"+ currentToken.getTokenID());
                }
                acceptTAndLookahead(Keywords.RPAREN);
                break;
                default:
                if (isUnaryOperator()) {
                    parseExpression();    
                } else {
                    parseError("unexpected token:"+ currentToken.getTokenID());
                }
            }
            if (!isBinaryOperator())
                break;
            acceptTAndLookahead();
        }       
    }

    public boolean isUnaryOperator()
    {
        int kind = currentToken.getKind();
        if ((kind == Keywords.NEGATION) ||
            (kind == Keywords.MINUS)) 
            return (true);
        return (false);
    }
    
    public boolean isBinaryOperator()
    {
        int kind = currentToken.getKind();
        if ((kind >= Keywords.BECOMES) && (kind <= Keywords.NEQUALS)) 
            return (true);
        return (false);
    }


    public void parseError( String str, boolean exitOnError)
    {
        System.out.println("Parse Error " + str);
        if (exitOnError)
            System.exit(4);
    }

    public void parseError( String str) 
    {
        System.out.println("Parse Error " + str);
        System.exit(4);
    }
}
