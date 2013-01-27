package miniJava.SyntacticAnalyzer;

import java.io.*;
import java.util.*;

public class Scanner 
{

    private SourceFile source;
    private StringBuffer buffer;
    private char scannedSymbol;
    private boolean bufferSym;
    private Keywords keywordsTable;
    
    public Scanner() 
    {
        bufferSym = true;
    }
    
    public Scanner(String fileName) 
    {
        source  = new SourceFile(fileName);
        scannedSymbol = source.scanSymbol();
        keywordsTable = new Keywords();
        bufferSym = true;
    }
    
    public Token scanToken()
    {
        int kind = -1;
        Integer obj = null;
        buffer = new StringBuffer();
        SourcePosition pos = new SourcePosition(source.getCurrentlineNum());
        kind = scan();
        pos.setFinish(source.getCurrentlineNum());
        if (kind == Keywords.IDENTIFIER)
            if ((obj = keywordsTable.get(buffer.toString())) != null) {
                kind = obj.intValue();
        }
        return (new Token(kind, buffer.toString(), pos));
    }

    public void acceptSymbol()
    {
            if (bufferSym)
                buffer.append(scannedSymbol);
            scannedSymbol =  source.scanSymbol();
    }

    public boolean scanComments()
    {
        bufferSym = false;
        if (scannedSymbol == '/') {
            do {
                acceptSymbol();
            } while (scannedSymbol != SourceFile.NEWLINE);
            acceptSymbol();
        } else if (scannedSymbol == '*') {
            while (true) { 
                acceptSymbol();
                if (scannedSymbol == '*') {
                    acceptSymbol();
                    if (scannedSymbol == '/') {
                        acceptSymbol();
                        break;
                    }
                }
            }
        } else {
            bufferSym = true;
            return (false);
        }
        buffer = new StringBuffer();
        bufferSym = true;
        return (true);
    }

    public void scanWhiteSpaceChars()
    {
        if (scannedSymbol == ' ' ||
            scannedSymbol == '\t'||
            scannedSymbol == '\n') {
            bufferSym = false;
            while (true) {
                acceptSymbol();
                if ((scannedSymbol != ' ')  &&
                    (scannedSymbol != '\t') &&
                    (scannedSymbol != '\n')) {
                    break;
                }
            }
            bufferSym = true;
        }
    }
    
    public int scan() 
    {
        scanWhiteSpaceChars();
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
                return (Keywords.RPAREN);
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
            case ('-'):
            case ('*'):
                acceptSymbol();
                return (Keywords.OPERATOR);
            case ('/'):
                acceptSymbol();
                if (scanComments())
                    return (scan());
                else 
                    return (Keywords.OPERATOR); 
            case ('&'):
                acceptSymbol();
                if (scannedSymbol == '&') {
                    acceptSymbol();
                    return (Keywords.OPERATOR);        
                } else {
                    return (Keywords.ERROR);
                }   
            case ('|'):
                acceptSymbol();
                if (scannedSymbol == '|') {
                    acceptSymbol();
                    return (Keywords.OPERATOR);      
                } else {
                    return (Keywords.ERROR);
                } 
            case ('!'):
            case ('>'):
            case ('<'):
            case ('='):
                acceptSymbol();
                if (scannedSymbol == '=') {
                    acceptSymbol();
                    return (Keywords.OPERATOR);       
                } else {
                    return (Keywords.OPERATOR);
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
                             Character.isLetter(scannedSymbol));
                    return (Keywords.IDENTIFIER); 
                } else {
                    acceptSymbol();
                    System.out.println("undefined character");
                    return (Keywords.ERROR);
                }
            }    
        }
    }  
