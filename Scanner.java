
package miniJava;

import java.io.*;
import java.util.*;

public class Scanner 
{

    private InputStreamReader isr;
    private Reader in;
    private int ch;
    private boolean lookahead = false; //this tells us whether we have already read an extra character from input
    private int line_num = 0;
    
    public Scanner() 
    {
        super();    
    }
    
    public Scanner(String file) 
    {
        try 
        {
            isr = new InputStreamReader(new FileInputStream(file), "US-ASCII");
            in = new BufferedReader(isr);
        } 
        catch (IOException ioException) 
        {
            System.err.println("Error Opening File");
        }
    
    }

    public void  scanToken() 
    {
        StringBuffer buffer = new StringBuffer();

        while (true)
        {
            if (lookahead) {
                lookahead = false;
                if (ch == -1) {
                    //return EOT token from here
                    return;
                }
            } else {
                if ((ch = in.read()) != -1)
                    break; //need to add end of input token here.
            }
            //need to replace breaks with returns
            switch (ch) {
                case ('\t'):
                case (' '):
                    break;
                case ('\n'):
                    line_num++;
                    break;
                case (']'):
                    break;
                case ('['):
                    break;
                case ('+'):
                    break;
                case ('-'):
                    break;
                case ('*'):
                    break;
                case ('!'):
                    buffer.append(ch);
                    if ((ch = in.read()) != -1) {
                        if (ch == '=') {
                            buffer.append(ch);
                            //return the token !=
                            return;
                        }
                    }
                    lookahead = true;
                    //return the token !.
                    return;
                case ('&'):
                    buffer.append(ch);
                    if ((ch = in.read()) != -1) {
                        if (ch == '&') {
                            //add the token part.
                            return;    
                        }
                    }
                    lookahead = true;
                    //return error token here.
                    return;
                case ('|'):
                    buffer.append(ch);
                    if ((ch = in.read()) != -1) {
                        if (ch == '|') {
                            //add the token part.
                            return;
                        }
                    }
                    lookahead = true;
                    //return error token here.
                    return;
                case ('>'):
                    buffer.append(ch);
                    if ((ch = in.read()) != -1) {
                        if (ch == '=') {
                            buffer.append(ch);
                            //return the token >=
                            return;
                        }
                    }
                    lookahead = true;
                    //return the token >
                    return;
                case ('<'):
                    buffer.append(ch);
                    if ((ch = in.read()) != -1) {
                        if (ch == '=') {
                            buffer.append(ch);
                            //return the token <=
                            return;
                        }
                    }
                    lookahead = true;
                    //return the token <
                    return;
                case ('='):
                    buffer.append(ch);
                    if ((ch = in.read()) != -1) {
                        if (ch == '=') {
                            buffer.append(ch);
                            //return the token ==
                            return;
                        }
                    }
                    lookahead = true;
                    //return the token =
                    return;
                case ('/'):
                    System.out.println("dfklgmdfgm");
                    buffer.append(ch);
                    if ((ch = in.read()) != -1)
                        
                    break;
                 



            }    

        }
    }
}  
