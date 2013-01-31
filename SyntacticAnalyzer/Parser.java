/*
 * File: Parser.java
 * Author: Rajesh Gopidi
 * PID:    720367703
 * Course : COMP520
 */

package miniJava.SyntacticAnalyzer;

import java.io.*;
import java.util.*;

/*
 * Parser Grammar:
 *
 *      Program  ::=  (ClassDeclaration)* eot
 *      ClassDeclaration  ::= class id {(FieldDeclaration | MethodDeclaration)*}
 *      FieldDeclaration  ::= Declarators id;
 *      MethodDeclaration  ::= Declarators id (ParameterList? ) {
 *                                             Statement* (return Expression ;)? }
 *      Declarators  ::= (public | private)? static?  Type
 *      Type  ::=  PrimType |  ClassType  |  ArrType  
 *      PrimType ::=   int | boolean  | void
 *      ClassType ::=  id 
 *      ArrType  ::=  ( int | ClassType ) []
 *      ParameterList  ::=  Type id (, Type id)*
 *      ArgumentList  ::=  Expression ( , Expression)*
 *      Reference  ::=  ( this | id ) ( . id )*
 *      Statement  ::=  { Statement* }
 *                      |  Type id = Expression ;
 *                      |  Reference ([ Expression ])?  = Expression ;
 *                      |  Reference ( ArgumentList? ) ;
 *                      |  if ( Expression ) Statement (else Statement)? 
 *                      |  while ( Expression ) Statement
 * 
 *      Expression ::=  Reference ( [ Expression ] )?
 *                      |  Reference ( ArgumentList? ) 
 *                      |  unop Expression
 *                      |  Expression binop Expression
 *                      |  ( Expression )
 *                      |  num  | true | false
 *                      |  new (id ( ) | int [ Expression ]  | id [ Expression ] )
 *      
 */         

public class Parser 
{
    // A variable to reference the current token object.
    Token currentToken;
    // An object of Scanner class to scan the source file
    Scanner lexicalAnalyzer;
    
    public Parser()
    {
    }

    public Parser(Scanner scanner)
    {
        this.lexicalAnalyzer = scanner;
    }

    /*
     * Method match()
     *
     * checks if the current token is of the type, which is passed 
     * as an argument.
     *
     * Returns true or false.
     */

    public boolean match(int kind)
    {
        if (currentToken.getKind() == kind)
            return (true);
        else 
            return (false);
    }

    /*
     * Method acceptTAndLookahead(int kind)
     *
     * Checks if the current token is of the type, which is passed 
     * as an argument. If it is, then retrieves next token. 
     * If it  doesn't match then it exists by throwing an 
     * an appropriate error.
     *
     * Returns void.
     */

    public void acceptTAndLookahead(int kind)
    {
        if (match(kind)) {
            System.out.println("Accepting token: "+ currentToken.getTokenID());
            currentToken = lexicalAnalyzer.scanToken();
        } else {
            parseError("Expected token: " + Keywords.tokenTable[kind] + ", but found token: "+ currentToken.getTokenID());
        }
    }

    /*
     * Method acceptTAndLookahead()
     *
     * Retrieves the next token from the source file. 
     *
     * Returns void.
     */

    public void acceptTAndLookahead()
    {
        System.out.println("Accepting token: "+ currentToken.getTokenID());
        currentToken = lexicalAnalyzer.scanToken();
    }

    /*
     * Method parseFile()
     *
     * A call to the method parses the file till EOT
     * token is encountered.
     *
     * Returns void.
     */

    public void parseFile()
    {
        currentToken = lexicalAnalyzer.scanToken();

        while (!match(Keywords.EOT)) {
            parseClass();
        }
        System.out.println("Successfully parsed the file");
    }

    /*
     * Method parseClass()
     *
     * A call to the method parses a class definition.
     *
     * Returns void.
     */
    
    public void parseClass()
    {
        acceptTAndLookahead(Keywords.CLASS);
        acceptTAndLookahead(Keywords.IDENTIFIER);
        acceptTAndLookahead(Keywords.LCURLY);

        while (!match(Keywords.RCURLY)) {
            // call to Declaration parser
            parseDeclarator();
            acceptTAndLookahead(Keywords.IDENTIFIER);
            /* check to see if it is method variable declaration or
             * method definition.
             */
            if (match(Keywords.SEMICOLON))
                acceptTAndLookahead();
            else 
                parseRestOfMethodDeclaration();
        }
        acceptTAndLookahead(Keywords.RCURLY);
    } 

    /*
     * Method parseDeclarator()
     *
     * This routine parses access identifiers 
     * and static modifier.
     *
     * Returns void.
     */

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

    /*
     * Method parseType()
     *
     * This method parses data/Class/return type 
     * in variable/method declarations.
     *  
     * Returns void. 
     */    

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
                parseError("Method: parseType(), unexpected token " + currentToken.getTokenID());
        }
    }

    /*
     * Method parseRestOfMethodDeclaration()
     *
     * This method parses method definition excluding 
     * the return type and method identifier.
     *  
     * Returns void. 
     */

    public void parseRestOfMethodDeclaration() 
    {
        acceptTAndLookahead(Keywords.LPAREN);
        // code below parses the argument list
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
        // code below parses the body of the method.
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

    /*
     * Method parseStmt()
     *
     * This method parses a statement inthe
     * source program.
     *  
     * Returns void. 
     */

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
            // case for a statement starting with a reference 
            case (Keywords.THIS):
                parseIDReference();
                parseReference(true, false);
                break;
            // case for a statement which is a variable/field/array defintion 
            case (Keywords.INT):
                parseBrackets(false);
                acceptTAndLookahead(Keywords.IDENTIFIER); 
                parseRestOfAssignmentStmt(); 
                break;
            // case for a statement which is a variable/field defintion
            case (Keywords.BOOLEAN):
            case (Keywords.VOID):
                acceptTAndLookahead(Keywords.IDENTIFIER);
                parseRestOfAssignmentStmt(); 
                break;
            // case for a statement starting with a class identifier
            case (Keywords.IDENTIFIER):
                // check to see if it is a statement starting with a reference or a method call
                if (parseIDReference() || match(Keywords.LPAREN)) {
                    parseReference(true, false);
                // check to see if it is a class declaration.
                } else if (match(Keywords.LBRACKET)) {
                    acceptTAndLookahead();
                    if (match(Keywords.RBRACKET)) {
                        acceptTAndLookahead();
                        acceptTAndLookahead(Keywords.IDENTIFIER);
                        parseRestOfAssignmentStmt();
                    } else {
                        // case for a statement starting with an array reference.
                        parseReference(true, true);
                        System.out.println("in after return reference");
                    }
                } else {
                    // below code parses definition of a class instance variable.
                    if (match(Keywords.IDENTIFIER)) {
                        acceptTAndLookahead();
                        parseRestOfAssignmentStmt();
                    } else {
                        // parses a statement which is a reference assignment statement
                        parseReference(true, false);
                    }
                }
                break;
            // case for parsing of IF statement
            case (Keywords.IF):
                parseIFStatement();
                System.out.println("in after return IF");
                break;
            // case of parsing of WHILE statement
            case (Keywords.WHILE):
                acceptTAndLookahead(Keywords.LPAREN);
                parseExpression();
                acceptTAndLookahead(Keywords.RPAREN);
                parseStmt();
                break;
            default:
                parseError("Method parseStmt(), unexpected token " + currentToken.getTokenID());
        }

    }

    /*
     * Method parseIDReference()
     *
     * This method parses a part of a statement 
     * which accesses a field/method of an object
     * 
     * Returns true, if the statement includes a reference to a
     * field/method of an object. In other cases, it returns false.
     */

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

    /*
     * Method parseReference()
     *
     * This method parses a part of a statement 
     * which is a call to method of an object or an 
     * assignment of a value to a memeber of an array.
     * 
     */

    public void parseReference(boolean isCallFromParseStmt, boolean skip)
    {
        if (match(Keywords.LPAREN)) {
            acceptTAndLookahead();
            while (!match(Keywords.RPAREN)) {
                parseArgumentList();     
            }
            acceptTAndLookahead();
            // check to see if a statement or an expression needs to be parsed
            if (isCallFromParseStmt)
                acceptTAndLookahead(Keywords.SEMICOLON);
        } else {
            // skips the check if it is already done by the callee
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
            // check to see if a statement or an expression needs to be parsed
            if (isCallFromParseStmt) {
                acceptTAndLookahead(Keywords.BECOMES);
                parseExpression();
                acceptTAndLookahead(Keywords.SEMICOLON); 
            }
        }
    }

    /*
     * Method parseArgumentList()
     *
     * This method parses a list of arguments 
     * in a method call.
     * 
     */

    public void parseArgumentList()
    {
        parseExpression();
        while (match(Keywords.COMMA)) {
            acceptTAndLookahead();
            parseExpression();
        }
    }

    /*
     * Method parseRestOfAssignmentStmt()
     *
     * This method parses an assignment  
     * statement starting from equal to
     * symbol.
     * 
     */


    public void parseRestOfAssignmentStmt()
    {
        acceptTAndLookahead(Keywords.BECOMES);
        parseExpression();
        acceptTAndLookahead(Keywords.SEMICOLON);
    }

    /*
     * Method parseBrackets()
     *
     * This method parses square brackets within an   
     * array declaration/definition.
     * 
     */
 
    public void parseBrackets(boolean exitOnError)
    {
        if (match(Keywords.LBRACKET)) {
            acceptTAndLookahead();
            if (match(Keywords.RBRACKET))
                acceptTAndLookahead();
            else
                parseError("Method: parseBrackets(), Unxpectedtokentype: " + 
                           currentToken.getTokenID() + " expecting token: "+ 
                           Keywords.tokenTable[Keywords.RBRACKET]);  
        }
    }

    /*
     * Method parseExpression()
     *
     * This method parses an expression within  
     * a statement.
     * 
     */

    public void parseExpression()
    {
        int kind;
        while (true) {
            kind = currentToken.getKind();
            acceptTAndLookahead();
            switch (kind) {
            /* case for an expression which is a 
             * method call / reference to an array element
             */
            case (Keywords.THIS):
            case (Keywords.IDENTIFIER):
                parseIDReference();
                parseReference(false, false);
                break;
            // case for expression of the form (expr)
            case (Keywords.LPAREN):
                parseExpression();
                acceptTAndLookahead(Keywords.RPAREN);
                break;
            case (Keywords.NUMBER):
            case (Keywords.TRUE):
            case (Keywords.FALSE):
                break;
            // case for an expression of the "new ..."
            case (Keywords.NEW):
                // id () | id [expr]
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
                        parseError(" Method: parseExpression(), case (NEW (id)**), unexpected token: "
                                   + currentToken.getTokenID());
                    }
                    // int [expression]
                } else if (match(Keywords.INT)) {
                    acceptTAndLookahead();
                    if (match(Keywords.LBRACKET)) {
                        acceptTAndLookahead();
                        parseExpression();
                        acceptTAndLookahead(Keywords.RBRACKET);
                    } else {
                        parseError("Method: parseExpression(), case (NEW int[]), unexpected token: "
                                   + currentToken.getTokenID());
                    }
                } else { 
                    parseError("Method: parseExpression(), case (NEW xx), unexpected token: "
                               + currentToken.getTokenID());
                }
                break;  
                // a case for an expression of the form unop expr
                case (Keywords.NEGATION):
                case (Keywords.MINUS):
                    parseExpression();
                    break;
                default:
                    parseError("Method: parseExpression(), case (default), unexpected token: "
                               + currentToken.getTokenID());
            }
            // check to see if the expression is of the form expr * expr
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
        System.out.println("Parse Error, " + str);
        if (exitOnError)
            System.exit(4);
    }

    public void parseError( String str) 
    {
        System.out.println("Parse Error, " + str);
        System.exit(4);
    }
}
