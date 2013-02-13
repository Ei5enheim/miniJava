/*
 * File: Scanner.java
 * Author: Rajesh Gopidi
 * PID:    720367703
 * Course : COMP520
 */

/*
 *  Scanner  
 *
 * Grammar:
 *      
 *   num ::= digit digit*
 *   id  ::= sym(sym | digit)*
 *   digit ::= '0' | ... | '9'
 *   sym   :: = [a-zA-Z]
 *   op ::= '+' | '*' | '-' | '/' | "&&" | "||" | '>' | 
 *          '<' | "<=" | ">=" | '!' | "!=" | "==" | '='
 *
 */

package miniJava.SyntacticAnalyzer;

import java.io.*;
import java.util.*;
import miniJava.SyntacticAnalyzer.*;
/*
 * Class: Scanner
 *
 * This class defines methods that parse the input file based on the grammar stated above. 
 *
 */

public class Scanner 
{
    // Object to read the input file
    private SourceFile source;
    // Buffer to store the parsed characters
    private StringBuffer buffer;
    // holds the currently scanned symbol
    private char scannedSymbol;
    // variable to turn on/off the buffer
    private boolean bufferSym;
    // Table that holds the keywords and their token identifiers
    private Keywords keywordsTable;
    
    public Scanner() 
    {
        bufferSym = true;
    }
    
    public Scanner(String fileName) 
    {
        // opening the file
        source  = new SourceFile(fileName);
        scannedSymbol = source.scanSymbol();
        // call to initiate the keywords table.
        keywordsTable = new Keywords();
        bufferSym = true;
    }
    
    /*
     * Method: scanToken
     *
     * This routine scans the input file from where it left off last time
     * return a Token object to the caller. 
     *
     */
    public Token scanToken()
    {
        int kind = -1;
        Integer obj = null;
        buffer = new StringBuffer();
        // retrieving the line number of the current token in the source file
        SourcePosition pos = new SourcePosition(source.getCurrentlineNum());
        // calling the scanner to scan the input file.
        kind = scan(false);
        pos.setFinish(source.getCurrentlineNum());
        /* 
         * if the token is an Identifier then we check if it is a key word by
         * searching the Keywords table.
         *
         */
        if (kind == Keywords.IDENTIFIER)
            if ((obj = keywordsTable.get(buffer.toString())) != null) {
                kind = obj.intValue();
            }
        // if EOF is reached then it returns the token, '$' 
        if (kind == Keywords.EOT)
            buffer.append('$');
        else if (kind == Keywords.ERROR_COMMENTS)
            buffer.append("undisclosed comments");
        //System.out.println("returning token: " + buffer.toString());
        return (new Token(kind, buffer.toString(), pos));
    }

    public Token scanToken(boolean scanWhitespace)
    {
        int kind = -1;
        Integer obj = null;
        buffer = new StringBuffer();
        // retrieving the line number of the current token in the source file
        SourcePosition pos = new SourcePosition(source.getCurrentlineNum());
        // calling the scanner to scan the input file.
        kind = scan(scanWhitespace);
        pos.setFinish(source.getCurrentlineNum());
        /* 
         * if the token is an Identifier then we check if it is a key word by
         * searching the Keywords table.
         *
         */
        if (kind == Keywords.IDENTIFIER)
            if ((obj = keywordsTable.get(buffer.toString())) != null) {
                kind = obj.intValue();
            }
        // if EOF is reached then it returns the token, '$' 
        if (kind == Keywords.EOT)
            buffer.append('$');
        else if (kind == Keywords.ERROR_COMMENTS)
            buffer.append("undisclosed comments");
        //System.out.println("returning token: " + buffer.toString());
        return (new Token(kind, buffer.toString(), pos));
    }

    /* 
     * Method: acceptSymbol()
     * 
     * Reads the next from the source file after appending the
     * current symbol to the string in the buffer. 
     *
     */
    public void acceptSymbol()
    {
            if (bufferSym)
                buffer.append(scannedSymbol);
            //System.out.println("Accepted symbol" + scannedSymbol);
            scannedSymbol =  source.scanSymbol();
    }

    /*
     *  Method: scanComments()
     *
     *  This method skips through all the comments encountered and exits when a 
     *  non-comment symbol is encountered.
     *  First, it checks for symbols "//" or "/*" and scans through the file until it encounters 
     *  a '\n' character or "'*'/" symbol respectively. The method flushes the buffer before 
     *  returning.
     *  At any stage, if it encounters EOF, then it stops executing and returns.     
     */

    public int scanComments()
    {
        bufferSym = false;
        if (scannedSymbol == '/') {
            do {
                acceptSymbol();
            } while ((scannedSymbol != SourceFile.NEWLINE) && (scannedSymbol != SourceFile.EOI));
            if (scannedSymbol == SourceFile.EOI) {
                return (1);
            } else {
                acceptSymbol();
            }
        } else if (scannedSymbol == '*') {
            acceptSymbol();
            while (true) { 
                //System.out.println("scanned symbol" + scannedSymbol);
                //acceptSymbol();
                if (scannedSymbol == '*') {
                    acceptSymbol();
                    if (scannedSymbol == '/') {
                        acceptSymbol();
                        break;
                    } else if (scannedSymbol == SourceFile.EOI) {
                        return (2);
                    }
                } else if (scannedSymbol == SourceFile.EOI) {
                    return (2);
                } else {
                    acceptSymbol();
                }
            }
        } else {
            bufferSym = true;
            return (0);
        }
        buffer = new StringBuffer();
        bufferSym = true;
        return (1);
    }

    /*
     * Method: scanWhiteSpaceChars() 
     *
     *  This method discards all the whitespace characters 
     *  encountered in the source file.
     */

    public boolean scanWhiteSpaceChars()
    {
        bufferSym = false;
        boolean rv = false;
        while (scannedSymbol == ' ' ||
               scannedSymbol == '\t'||
               scannedSymbol == '\r'||
               scannedSymbol == '\n') {
            acceptSymbol();
            rv = true;
        }
        bufferSym = true;
        return (rv);
    }
   
    /*
     * Method: scan()
     *
     * This function scans through the input file one symbol
     * at a time [ignores whitespace characters and comments].
     * It exits  when a valid token is accumulated in the buffer
     * or EOF is encountered.
     *
     * Returns token type.
     */ 
    public int scan(boolean scanWhitespace) 
    {
        boolean return_val;
        return_val = scanWhiteSpaceChars(); 
        if (scanWhitespace) {
            if (return_val)
                return (Keywords.WHITESPACE);
        } 
        switch (scannedSymbol) {
            case ('{'):
                acceptSymbol();
                return (Keywords.LCURLY);
            case ('}'):
                acceptSymbol();
                return (Keywords.RCURLY);
            case (']'):
                acceptSymbol();
                return (Keywords.RBRACKET);
            case (')'):
                acceptSymbol();
                return (Keywords.RPAREN);
            case('('):
                acceptSymbol();
                return (Keywords.LPAREN);
            case(';'):
                acceptSymbol();
                return (Keywords.SEMICOLON);
            case ('['):
                acceptSymbol();
                return (Keywords.LBRACKET);
            case (','):
                acceptSymbol();
                return (Keywords.COMMA);
            case ('.'):
                acceptSymbol();
                return (Keywords.DOT);
            case ('+'):
                acceptSymbol();
                return (Keywords.PLUS);
            case ('-'):
                acceptSymbol();
                if (scanWhiteSpaceChars()) {
                    return (Keywords.MINUS);
                } else if (scannedSymbol != '-') {
                    return (Keywords.MINUS);
                } else {
                    acceptSymbol();
                    return (Keywords.ERROR);
                }
            case ('*'):
                acceptSymbol();
                return (Keywords.INTO);
            case ('/'):
                acceptSymbol();
                int rv = scanComments();
                if (rv == 1)
                    return (scan(false));
                else if (rv == 0)
                    return (Keywords.DIVISION);
                else 
                    return (Keywords.ERROR_COMMENTS); 
            case ('&'):
                acceptSymbol();
                if (scannedSymbol == '&') {
                    acceptSymbol();
                    return (Keywords.AND);        
                } else {
                    return (Keywords.ERROR);
                }   
            case ('|'):
                acceptSymbol();
                if (scannedSymbol == '|') {
                    acceptSymbol();
                    return (Keywords.OR);      
                } else {
                    return (Keywords.ERROR);
                } 
            case ('!'):
                acceptSymbol();
                if (scannedSymbol == '=') {
                    acceptSymbol();
                    return (Keywords.NEQUALS);     
                } else {
                    return (Keywords.NEGATION);
                }
            case ('>'):
                acceptSymbol();
                if (scannedSymbol == '=') {
                    acceptSymbol();
                    return (Keywords.GTHANEQT);     
                } else {
                    return (Keywords.GTHAN);
                }
            case ('<'):
                acceptSymbol();
                if (scannedSymbol == '=') {
                    acceptSymbol();
                    return (Keywords.LTHANEQT);     
                } else {
                    return (Keywords.LTHAN);
                }
            case ('='):
                acceptSymbol();
                if (scannedSymbol == '=') {
                    acceptSymbol();
                    return (Keywords.EQUALS);       
                } else {
                    return (Keywords.BECOMES);
                }
            case (SourceFile.EOI):
                return (Keywords.EOT); 
            default:
                if (Character.isDigit(scannedSymbol)) {
                    do {
                        acceptSymbol();
                    } while (Character.isDigit(scannedSymbol));
                    return (Keywords.NUMBER);

                } else if (Character.isLetter(scannedSymbol)) {
                    do {
                        acceptSymbol();
                    } while (Character.isDigit(scannedSymbol) ||
                             Character.isLetter(scannedSymbol)||
                             scannedSymbol == '_');
                    return (Keywords.IDENTIFIER); 
                } else {
                    acceptSymbol();
                    System.out.println("undefined character");
                    return (Keywords.ERROR);
                }
            }    
        }
    }  
