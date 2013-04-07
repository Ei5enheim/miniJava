/*
 * File: SourceFile.java
 * Author: Rajesh Gopidi
 * PID:    720367703
 * Course : COMP520
 */

package miniJava.SyntacticAnalyzer;
import java.io.*;
import java.util.*;
import miniJava.SyntacticAnalyzer.*;

public class SourceFile
{

    public static final char NEWLINE = '\n';
    public static final char EOI = '\u0000';
    /* An object to read bytes and decode them into
     * characters using a specific charset.
     */ 
    private InputStreamReader isr;
    // An object to read characters from a char stream. 
    private Reader in;
    // A variable to store the scanned character
    private int ch;
    /* A variable to keep track of the line number of
     * character being scanned from the source file.
     */
    private int currentlineNum = 1;

    public SourceFile()
    {
        isr = null;
        in = null;
    }
    
    public SourceFile(String fileName)
    {
        
        try
        {
            // Using the ASCII charset to decode the byte stream.
            isr = new InputStreamReader(new FileInputStream(fileName), "US-ASCII");
            // creating a new BufferedReader object 
            in = new BufferedReader(isr);
        }
        catch (IOException ioException)
        {
            System.err.println("Error Opening File");
            isr = null;
            in = null;
            System.exit(4);
        }

    }

    /*
     * Method scanSymbol()
     * 
     * Reads a character from the input file and returns it.
     *
     */
    public char scanSymbol()
    {
        try 
        {
            ch = in.read();
        }
        catch (IOException ioException) 
        {
            System.out.println("Error reading next symbol");
            return (EOI);
        }
        // Check for EOF 
        if (ch == -1) {
            return (EOI);
        } else if ((char) ch == NEWLINE) {
            currentlineNum++;
            return (NEWLINE);
        } else {
            return ((char)ch);
        }
    }

    /*
     *  Method: getCurrentlineNum()
     *
     *  Returns the line number of the last symbol
     *  scanned in the input source file.
     *
     */
    public int getCurrentlineNum()
    {
        return currentlineNum;
    }
   
    /*
     * Method close()
     *
     *  Closes all open streams.
     */ 
    public void close () 
    {
        try
        {
            in.close();
            isr.close();
        }
        catch (IOException ioException)
        {
            System.err.println("error close stream");
        }

    }
}
