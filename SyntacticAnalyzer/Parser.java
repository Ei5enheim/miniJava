/*
 * File: Parser.java
 * Author: Rajesh Gopidi
 * PID:    720367703
 * Course : COMP520
 */

package miniJava.SyntacticAnalyzer;

import java.io.*;
import java.util.*;
import miniJava.SyntacticAnalyzer.*;
import miniJava.AbstractSyntaxTrees.*;

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
    boolean debug = true; 
    FieldDecl fieldDecl;
    ParameterDeclList paraList;

    public Parser()
    {
    }

    public Parser(Scanner scanner)
    {
        this.lexicalAnalyzer = scanner;
        fieldDecl = new FieldDecl(false, false, null, null, null);
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

    public boolean acceptTAndLookahead(boolean scanWhitespace)
    {
        System.out.println("Accepting token: "+ currentToken.getTokenID());
        currentToken = lexicalAnalyzer.scanToken(scanWhitespace);
        if (currentToken.getKind() == Keywords.WHITESPACE) {
            return (true);
        } else {
            return (false);
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
        ClassDeclList classList = new ClassDeclList();
        AST pac = new miniJava.AbstractSyntaxTrees.Package(classList, new SourcePosition());

        currentToken = lexicalAnalyzer.scanToken();
        while (!match(Keywords.EOT)) {
            ClassDecl classDecl = parseClass();
            classList.add(classDecl);
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
    
    public ClassDecl parseClass()
    {
        ClassDecl classDecl = null;
        String className = null ;
        FieldDeclList fieldDeclList = null;
        MethodDeclList methodDeclList = null;

        acceptTAndLookahead(Keywords.CLASS);
        className = currentToken.getTokenID();
        acceptTAndLookahead(Keywords.IDENTIFIER);
        acceptTAndLookahead(Keywords.LCURLY);

        fieldDeclList  = new FieldDeclList();
        methodDeclList = new MethodDeclList(); 

        while (!match(Keywords.RCURLY)) {
            // call to Declaration parser
            fieldDecl = parseDeclarator();
            fieldDecl.posn = currentToken.pos;
            fieldDecl.name = currentToken.getTokenID();
            acceptTAndLookahead(Keywords.IDENTIFIER);
            /* check to see if it is method variable declaration or
             * method definition.
             */
            if (match(Keywords.SEMICOLON)) {
                acceptTAndLookahead();
                fieldDeclList.add(fieldDecl);
            } else { 
                parseRestOfMethodDeclaration(fieldDecl);
            }
        }
        acceptTAndLookahead(Keywords.RCURLY);

        return (classDecl);
    } 

    /*
     * Method parseDeclarator()
     *
     * This routine parses access identifiers 
     * and static modifier.
     *
     * Returns void.
     */

    public FieldDecl parseDeclarator()
    {
        int kind = currentToken.getKind();
        if (kind == Keywords.PRIVATE) {
            fieldDecl.isPrivate = true;
            acceptTAndLookahead();
            kind = currentToken.getKind();
        } else {
            fieldDecl.isPrivate = false;
        }
        if (kind == Keywords.PUBLIC) {
            acceptTAndLookahead();
            kind = currentToken.getKind();
        }
        if ( kind == Keywords.STATIC) { 
             acceptTAndLookahead();
             fieldDecl.isStatic = true;
        } else {
            fieldDecl.isStatic = false;
        }
        fieldDecl.type = parseType();

        return (fieldDecl);
    }

    /*
     * Method parseType()
     *
     * This method parses data/Class/return type 
     * in variable/method declarations.
     *  
     * Returns void. 
     */    

    public Type parseType()
    {
        Type typ = null;
        
        switch (currentToken.getKind()) {
            case (Keywords.INT):
                typ = new BaseType(TypeKind.INT, currentToken.pos); 
                acceptTAndLookahead();
                break; 
            case (Keywords.IDENTIFIER):
                typ = new ClassType(currentToken.getTokenID(), currentToken.pos);
                acceptTAndLookahead();
                break;
            case (Keywords.BOOLEAN):
                typ = new BaseType(TypeKind.BOOLEAN, currentToken.pos);
                acceptTAndLookahead();
                return (typ);
            case (Keywords.VOID):
                typ = new BaseType(TypeKind.VOID, currentToken.pos);
                acceptTAndLookahead();
                return (typ);
            default:
                parseError("Method: parseType(), unexpected token " + currentToken.getTokenID());
        }
        if (match(Keywords.LBRACKET)) {
            acceptTAndLookahead();
            acceptTAndLookahead(Keywords.RBRACKET);
            typ = new ArrayType(typ, currentToken.pos);
        }              
        return (typ);
    }

    /*
     * Method parseRestOfMethodDeclaration()
     *
     * This method parses method definition excluding 
     * the return type and method identifier.
     *  
     * Returns void. 
     */

    public void parseRestOfMethodDeclaration(FieldDecl fieldDecl) 
    {
        Type typ;
        ParameterDecl paraDecl = null;
        String name = null;
        SourcePosition pos = null;
       
        paraList = new ParameterDeclList();
        acceptTAndLookahead(Keywords.LPAREN);
        // code below parses the argument list
        if (!match(Keywords.RPAREN)) {
            while (true) {
                typ = parseType();
                pos = currentToken.pos;
                name = currentToken.getTokenID();
                acceptTAndLookahead(Keywords.IDENTIFIER);
                paraDecl = new ParameterDecl(typ, name, pos);
                paraList.add(paraDecl);
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
            if (match(Keywords.RETURN)) {
                acceptTAndLookahead();
                parseExpression();
                acceptTAndLookahead(Keywords.SEMICOLON);
                break;
            } else {
                parseStmt();
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

    public boolean parseIDReference()
    {
        return true;
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

    public boolean parseIDReference(IdentifierList idList)
    {
        boolean retValue = false;
        String name = null;
        SourcePosition pos = null;

        while (match(Keywords.DOT)) {
            acceptTAndLookahead();
            pos = currentToken.pos;
            name = currentToken.getTokenID();
            acceptTAndLookahead(Keywords.IDENTIFIER);
            idList.add(new Identifier(name, pos));
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

     public void parseReference (boolean isCallFromParseStmt, boolean skip)
     {

     }

    /*
     * Method parseReference()
     *
     * This method parses a part of a statement 
     * which is a call to method of an object or an 
     * assignment of a value to a memeber of an array.
     * 
     */

    public AST parseReference (boolean isCallFromParseStmt, boolean skip, Reference ref)
    {
        Expression expr = null;
        ExprList exprList = null;
        IndexedRef indxRef = null;
        AST ast = null;

        if (match(Keywords.LPAREN)) {
            acceptTAndLookahead();
            exprList = new ExprList();
            if (!match(Keywords.RPAREN)) {
                parseArgumentList(exprList);     
            }
            acceptTAndLookahead(Keywords.RPAREN);
            // check to see if a statement or an expression needs to be parsed
            if (isCallFromParseStmt) {
                acceptTAndLookahead(Keywords.SEMICOLON);
                ast = new CallStmt(ref, exprList, ref.posn);
            } else {
                ast = new CallExpr(ref, exprList, ref.posn);
            }
        } else {
            // skips the check if it is already done by the callee
            if (!skip) {
                if (match(Keywords.LBRACKET)) {
                    acceptTAndLookahead();    
                    expr = parseExpression();
                    acceptTAndLookahead(Keywords.RBRACKET);
                    indxRef = new IndexedRef(ref, expr, ref.posn);
                    ast = new RefExpr(indxRef, ref.posn);            
                }
            } else { 
                expr = parseExpression();
                acceptTAndLookahead(Keywords.RBRACKET);
                ast = (AST) new RefExpr(ref, ref.posn);
            }
            // check to see if a statement or an expression needs to be parsed
            if (isCallFromParseStmt) {
                acceptTAndLookahead(Keywords.BECOMES);
                expr = parseExpression();
                acceptTAndLookahead(Keywords.SEMICOLON); 
                //write stmt code
            }
        }
        return (ast);
    }

    /*
     * Method parseArgumentList()
     *
     * This method parses a list of arguments 
     * in a method call.
     * 
     */

    public void parseArgumentList(ExprList exprList)
    {
        Expression expr = null;
    
        expr = parseExpression();
        exprList.add(expr);
        while (match(Keywords.COMMA)) {
            acceptTAndLookahead();
            expr = parseExpression();
            exprList.add(expr);
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

    public Expression parseExpression()
    {
        Expression expr1 = null, expr2 = null;
        Operator op = null;

        System.out.println("In parse expression");

        expr1 = parseExpressionL1();

        while (match(Keywords.OR)) {
            op = new Operator(currentToken.getTokenID(), currentToken.pos);
            acceptTAndLookahead();
            expr2 = parseExpressionL2();
            expr1 = new BinaryExpr (op, expr1, expr2, expr1.posn);
        }
        return (expr1);
    }

    public Expression parseExpressionL1() 
    {
        Expression expr1 = null, expr2 = null;
        Operator op = null;

        if (debug)
            System.out.println("In parse expressionL1");

        expr1 = parseExpressionL2();
        while (match(Keywords.AND)) {
            op = new Operator(currentToken.getTokenID(), currentToken.pos);
            acceptTAndLookahead();
            expr2 = parseExpressionL2();
            expr1 = new BinaryExpr (op, expr1, expr2, expr1.posn);
        }
        return (expr1);
    }

    public Expression parseExpressionL2()
    {

        Expression expr1 = null, expr2 = null;
        Operator op = null;

        if (debug)
            System.out.println("In parse expressionL2");

        expr1 = parseExpressionL3();
        
        while (match(Keywords.EQUALS) ||
               match(Keywords.NEQUALS)) {
            op = new Operator(currentToken.getTokenID(), currentToken.pos);
            acceptTAndLookahead();
            expr2 = parseExpressionL3();
            expr1 = new BinaryExpr (op, expr1, expr2, expr1.posn);
        }
        return (expr1);
    }

    public Expression parseExpressionL3()
    {
        Expression expr1 = null, expr2 = null;
        Operator op = null;

        if (debug)
            System.out.println("In parse expressionL3");

        expr1 = parseExpressionL4();
        while (match(Keywords.GTHAN) ||
               match(Keywords.LTHAN) ||
               match(Keywords.GTHANEQT) ||
               match(Keywords.LTHANEQT)) {
            op = new Operator(currentToken.getTokenID(), currentToken.pos);
            acceptTAndLookahead();
            expr2 = parseExpressionL4();
            expr1 = new BinaryExpr (op, expr1, expr2, expr1.posn);
        }
        return (expr1);
    }   

    public Expression parseExpressionL4()
    {
        Expression expr1 = null, expr2 = null;
        Operator op = null;
        
        if (debug)
            System.out.println("In parse expressionL4");
    
        expr1 = parseExpressionL5();
        while (match(Keywords.PLUS) ||
               match(Keywords.MINUS)) {
            op = new Operator(currentToken.getTokenID(), currentToken.pos);    
            acceptTAndLookahead();
            expr2 = parseExpressionL5();
            expr1 = new BinaryExpr (op, expr1, expr2, expr1.posn);
        }
        return (expr1);
    }   

    public Expression parseExpressionL5()
    {
        Expression expr1 = null, expr2 = null;
        Operator op = null;

        if (debug)
            System.out.println("In parse expressionL5");
    
        expr1 = parseExpressionL6();
    
        while (match(Keywords.DIVISION) ||
               match(Keywords.INTO)) {
            op = new Operator (currentToken.getTokenID(), currentToken.pos);
            acceptTAndLookahead();
            expr2 = parseExpressionL6();
            expr1 = new BinaryExpr (op, expr1, expr2, expr1.posn);
        }
        return (expr1);
    }

    public Expression parseExpressionL6()
    {
        Operator op = null;
        Expression expr = null;
        int kind = -1;

        if (debug)
            System.out.println("In parse expressionL6");
        
        kind = currentToken.getKind();
        if ((kind == Keywords.MINUS) || 
            (kind == Keywords.NEGATION)) {
            op = new Operator (currentToken.getTokenID(), currentToken.pos);
            acceptTAndLookahead();
            expr = new UnaryExpr (op, parseExpressionL6(), currentToken.pos); 
        } else {
            expr = (Expression) parseIDsInExpression();
        }
        return (expr);
    }

    public AST parseIDsInExpression()
    {
        Expression expr = null;
        int kind;
        SourcePosition pos = null;
        IdentifierList idList = null;
        Reference ref = null;
        String name = null;
        AST ast = null;

        if (debug)
            System.out.println("In parseIDsInExpression");

        kind = currentToken.getKind();
        pos = currentToken.pos;
        name = currentToken.getTokenID(); 
        System.out.print("beforehand ");
        acceptTAndLookahead();
        switch (kind) {
            /* case for an expression which is a 
             * method call / reference to an array element
             */
        case (Keywords.THIS):
            idList = new IdentifierList();
            parseIDReference(idList);
            ast = new RefExpr (new QualifiedRef(true, idList, pos), pos);
            break;
        case (Keywords.IDENTIFIER):
            idList = new IdentifierList();
            idList.add(new Identifier(name, pos));
            parseIDReference(idList);
            ref = new QualifiedRef(false, idList, pos);
            ast = parseReference(false, false, ref);
            break;
            // case for expression of the form (expr)
        case (Keywords.LPAREN):
            ast = parseExpression();
            acceptTAndLookahead(Keywords.RPAREN);
            break;
        case (Keywords.NUMBER):
            ast = new LiteralExpr (new IntLiteral (name, pos), pos); 
            break;
        case (Keywords.TRUE):
        case (Keywords.FALSE):
            ast = new LiteralExpr (new BooleanLiteral(name, pos), pos);
            break;
            // case for an expression of the "new ..."
        case (Keywords.NEW):
            // id () | id [expr]
            if (match(Keywords.IDENTIFIER)) {
                name = currentToken.getTokenID();
                pos = currentToken.pos;
                acceptTAndLookahead();
                if (match(Keywords.LPAREN)) {
                    acceptTAndLookahead();
                    acceptTAndLookahead(Keywords.RPAREN);
                    ast = new NewObjectExpr(new ClassType(name, pos), pos);
                } else if (match(Keywords.LBRACKET)) {
                    acceptTAndLookahead();
                    expr = parseExpression();
                    acceptTAndLookahead(Keywords.RBRACKET);
                    ast = new NewArrayExpr( new ClassType(name, pos), expr, pos);
                } else {
                    parseError(" Method: parseExpression(), case (NEW (id)**), unexpected token: "
                               + currentToken.getTokenID());
                }
                // int [expression]
            } else if (match(Keywords.INT)) {
                pos = currentToken.pos;
                acceptTAndLookahead();
                acceptTAndLookahead(Keywords.LBRACKET);
                expr =  parseExpression();
                acceptTAndLookahead(Keywords.RBRACKET);
                ast = new NewArrayExpr( new BaseType(TypeKind.INT, pos), expr, pos);
            } else { 
                parseError("Method: parseExpression(), case (NEW xx), unexpected token: "
                           + currentToken.getTokenID());
            }
            break;  
            // a case for an expression of the form unop expr
        default:
            parseError("Method: parseIDsInExpression(), case (default), unexpected token: "
                       +  Keywords.tokenTable[kind]);
        }
        return (ast);
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
        if ((kind >= Keywords.PLUS) && (kind <= Keywords.NEQUALS)) 
            return (true);
        return (false);
    }


    public void parseError(String str, boolean exitOnError)
    {
        System.out.println("Parse Error, " + str);
        if (exitOnError)
            System.exit(4);
    }

    public void parseError(String str) 
    {
        System.out.println("Parse Error, " + str);
        System.exit(4);
    }
}
