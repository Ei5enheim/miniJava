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

    public boolean match(int tokenID)
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
            parseError("Expected token" + Keywords.tokenTable[tokenID] + ", but found token"+ currentToken.getTokenID());
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
            acceptTAndLookahead(Keywords.IDENTIFIER);
            if (match(Keywords.SEMICOLON))
                acceptTAndLookahead();
            else 
                parseRestOfMethodDeclaration();
        }
        acceptTAndLookahead(Keywords.RCURLY);
    } 

    public void parseDeclarator()
    {
        int kind = currentToken.getKind();
        if ((kind == Keywords.PRIVATE) ||
            (kind == Keywords.PUBLIC)) {
            acceptTAndLookahead();
            kind = currentToken.getKind();
        }
        if ( kind == Keywords.STATIC)
             acceptTAndLookahead();
        parseType();
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
                break;
            default:
                parseError("Method: parseType(), unexpected token type " + currentToken.getTokenID());

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
                break;
            }   
        }
        acceptTAndLookahead(Keywords.RCURLY);
    } 

    public void parseStmt() 
    {
        System.out.println("In statement");
        int kind = currentToken.getKind();
        acceptTAndLookahead();
        switch (kind) {
            case (Keywords.LCURLY):
                while (!match(Keywords.RCURLY)) {
                    parseStmt();
                    System.out.println("in after return Lcurly"); 
                }
                acceptTAndLookahead();
                break;
            case (Keywords.THIS):
                parseIDReference();
                parseReference(true, false);
                break;
            case (Keywords.INT):
                parseBrackets(false);
                acceptTAndLookahead(Keywords.IDENTIFIER); 
                parseRestOfAssignmentStmt(); 
                break;
            case (Keywords.BOOLEAN):
            case (Keywords.VOID):
                acceptTAndLookahead(Keywords.IDENTIFIER);
                parseRestOfAssignmentStmt(); 
                break;
            case (Keywords.IDENTIFIER):
                if (parseIDReference() || match(Keywords.LPAREN)) {
                    parseReference(true, false);
                } else if (match(Keywords.LBRACKET)) {
                    acceptTAndLookahead();
                    if (match(Keywords.RBRACKET)) {
                        acceptTAndLookahead();
                        acceptTAndLookahead(Keywords.IDENTIFIER);
                        parseRestOfAssignmentStmt();
                    } else {
                        parseReference(true, true);
                        System.out.println("in after return reference");
                    }
                } else {
                    if (match(Keywords.IDENTIFIER)) {
                        acceptTAndLookahead();
                        parseRestOfAssignmentStmt();
                    } else {
                        parseReference(true, false);
                    }
                }
                break;
            case (Keywords.IF):
                parseIFStatement();
                System.out.println("in after return IF");
                break;
            case (Keywords.WHILE):
                acceptTAndLookahead(Keywords.LPAREN);
                parseExpression();
                acceptTAndLookahead(Keywords.RPAREN);
                parseStmt();
                break;
            default:
                parseError("Method parseStmt(), unexpected token type " + currentToken.getTokenID());
        }

    }

    public boolean parseIDReference()
    {
        boolean retValue = false;

        while (match(Keywords.DOT)) {
            acceptTAndLookahead();
            acceptTAndLookahead(Keywords.IDENTIFIER);
            retValue = true;
        }
        return (retValue);
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

    public void parseReference(boolean isCallFromParseStmt, boolean skip)
    {
        if (match(Keywords.LPAREN)) {
            acceptTAndLookahead();
            while (!match(Keywords.RPAREN)) {
                parseArgumentList();     
            }
            acceptTAndLookahead();
            if (isCallFromParseStmt)
                acceptTAndLookahead(Keywords.SEMICOLON);
        } else {
            if (!skip) {
                if (match(Keywords.LBRACKET)) {
                    acceptTAndLookahead();    
                    parseExpression();
                    acceptTAndLookahead(Keywords.RBRACKET);
                }
            } else { 
                parseExpression();
                acceptTAndLookahead(Keywords.RBRACKET);
            }
            if (isCallFromParseStmt) {
                acceptTAndLookahead(Keywords.BECOMES);
                parseExpression();
                acceptTAndLookahead(Keywords.SEMICOLON); 
            }
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
                parseError("Method: parseBrackets(), Unxpectedtokentype: " + currentToken.getTokenID() + " expecting token: "+ Keywords.tokenTable[Keywords.RBRACKET]);  
        }
    }

    public void parseExpression()
    {
        System.out.println("In parse Expression");
        int kind;
        while (true) {
            kind = currentToken.getKind();
            acceptTAndLookahead();
            switch (kind) {
            case (Keywords.THIS):
            case (Keywords.IDENTIFIER):
                parseIDReference();
                parseReference(false, false);
                break;
            case (Keywords.LPAREN):
                parseExpression();
                acceptTAndLookahead(Keywords.RPAREN);
                break;
            case (Keywords.NUMBER):
            case (Keywords.TRUE):
            case (Keywords.FALSE):
                break;
            case (Keywords.NEW):
                if (match(Keywords.IDENTIFIER)) {
                    acceptTAndLookahead();
                    if (match(Keywords.LPAREN)) {
                        acceptTAndLookahead();
                        acceptTAndLookahead(Keywords.RPAREN);
                    } else if (match(Keywords.LBRACKET)) {
                        acceptTAndLookahead();
                        parseExpression();
                        acceptTAndLookahead(Keywords.RBRACKET);
                    } else {
                        parseError(" Method: parseExpression(), case (NEW (id)**), unexpected token: "+ currentToken.getTokenID());
                    }
                } else if (match(Keywords.INT)) {
                    acceptTAndLookahead();
                    if (match(Keywords.LBRACKET)) {
                        acceptTAndLookahead();
                        parseExpression();
                        acceptTAndLookahead(Keywords.RBRACKET);
                    } else {
                        parseError("Method: parseExpression(), case (NEW int[]), unexpected token: "+ currentToken.getTokenID());
                    }
                } else { 
                    parseError("Method: parseExpression(), case (NEW xx), unexpected token: "+ currentToken.getTokenID());
                }
                break;
                default:
                if (isUnaryOperator(kind)) {
                    parseExpression();    
                } else {
                    parseError("Method: parseExpression(), case (default), unexpected token: "+ currentToken.getTokenID());
                }
            }
            if (!isBinaryOperator())
                break;
            acceptTAndLookahead();
        }       
    }

    public boolean isUnaryOperator(int kind)
    {
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
