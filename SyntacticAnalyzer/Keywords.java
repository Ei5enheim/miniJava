package miniJava.SyntacticAnalyzer;

import java.io.*;
import java.util.*;

public class Keywords 
{

    private final Hashtable<String, Integer> keywords;

    public Keywords()
    {
        keywords = new Hashtable<String, Integer>();
        addKeywords();
    }
    
    private void addKeywords() 
    {
        for (int i = BOOLEAN; i <= WHILE; i++) {
            keywords.put(tokenTable[i], new Integer(i));
        }
    }
    
    public Integer get(String str)
    {
        return (keywords.get(str));

    }

    public static final int 

    // literals, identifiers, operators...
    NUMBER  = 0,
    IDENTIFIER  = 1,
    OPERATOR    = 2,

    // reserved words - must be in alphabetical order...
    BOOLEAN       = 3,
    CLASS         = 4,
    ELSE          = 5,
    FALSE         = 6,
    IF            = 7,
    INT           = 8,
    NEW           = 9,
    PRIVATE       = 10,
    PUBLIC        = 11,
    RETURN        = 12,
    STATIC        = 13,
    THIS          = 14,
    TRUE          = 15,
    VOID          = 16,
    WHILE         = 17,
    
    // punctuation...
    DOT         = 18,
    SEMICOLON   = 19,
    COMMA       = 20,

    // brackets...
    LPAREN      = 21,
    RPAREN      = 22,
    LBRACKET    = 23,
    RBRACKET    = 24,
    LCURLY      = 25,
    RCURLY      = 26,

    // special tokens...
    EOT         = 27,
    ERROR       = 28;

  private static String[] tokenTable = new String[] {
    "<num>",
    "<identifier>",
    "<operator>",
    "boolean",
    "class",
    "else",
    "false",
    "if",
    "int",
    "new",
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
    "<error>",
    ""
  };
}
