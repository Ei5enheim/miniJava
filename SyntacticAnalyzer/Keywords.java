/*
 * File: Keywords.java
 * Author: Rajesh Gopidi
 * PID:    720367703
 * Course : COMP520
 */

package miniJava.SyntacticAnalyzer;

import java.io.*;
import java.util.*;
import miniJava.SyntacticAnalyzer.*;
public class Keywords 
{
    // A Hashtable object to store the keywords 
    private final Hashtable<String, Integer> keywords;

    public Keywords()
    {
        // Initiating the Hashtable
        keywords = new Hashtable<String, Integer>();
        // call to add the keywords to the Hashtable.
        addKeywords();
    }
    
    private void addKeywords() 
    {
        /* runs through an array of String objects and 
         * adds them to the Hashtable.
         */
        for (int i = BOOLEAN; i <= WHILE; i++) {
            keywords.put(tokenTable[i], new Integer(i));
        }
    }
    /*
     * Method get
     *
     * Returns the key of a particular token spelling 
     *
     */ 
    public Integer get(String str)
    {
        return (keywords.get(str));

    }

    public static final int 

    // literals, identifiers, operators...
    NUMBER  = 0,
    IDENTIFIER  = 1,
    BECOMES     = 2,
    PLUS        = 3,
    MINUS       = 4,
    INTO        = 5,
    DIVISION    = 6,
    AND         = 7,
    OR          = 8,
    GTHAN       = 9,
    LTHAN       = 10,
    GTHANEQT    = 11,
    LTHANEQT    = 12,
    EQUALS      = 13,
    NEQUALS     = 14,
    NEGATION    = 15,
    
    // reserved words - must be in alphabetical order...
    BOOLEAN       = 16,
    CLASS         = 17,
    ELSE          = 18,
    FALSE         = 19,
    IF            = 20,
    INT           = 21,
    NEW           = 22,
    NULL          = 23,
    PRIVATE       = 24,
    PUBLIC        = 25,
    RETURN        = 26,
    STATIC        = 27,
    THIS          = 28,
    TRUE          = 29,
    VOID          = 30,
    WHILE         = 31,
    
    // punctuation...
    DOT         = 32,
    SEMICOLON   = 33,
    COMMA       = 34,

    // brackets...
    LPAREN      = 35,
    RPAREN      = 36,
    LBRACKET    = 37,
    RBRACKET    = 38,
    LCURLY      = 39,
    RCURLY      = 40,

    // special tokens...
    EOT         = 41,
    ERROR       = 42,
    ERROR_COMMENTS = 43,
    WHITESPACE = 44;

  public static String[] tokenTable = new String[] {
    "<num>",
    "<identifier>",
    "=",
    "+",
    "-",
    "*",
    "/",
    "&&",
    "||",
    ">",
    "<",
    ">=",
    "<=",
    "==",
    "!=",
    "!",
    "boolean",
    "class",
    "else",
    "false",
    "if",
    "int",
    "new",
    "null",
    "private",
    "public",
    "return",
    "static",
    "this",
    "true",
    "void",
    "while",
    ".",
    ";",
    ",",
    "(",
    ")",
    "[",
    "]",
    "{",
    "}",
    "$",
    "<error>",
    "<unclosed_comments>",
    " "
  };
}
