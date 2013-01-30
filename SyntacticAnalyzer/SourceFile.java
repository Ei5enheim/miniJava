package miniJava.SyntacticAnalyzer;
import java.io.*;
import java.util.*;

public class SourceFile
{

    public static final char NEWLINE = '\n';
    public static final char EOI = '\u0000';
    private InputStreamReader isr;
    private Reader in;
    private int ch;
    private int currentlineNum;

    public SourceFile()
    {
        isr = null;
        in = null;
    }
    
    public SourceFile(String fileName)
    {
        try
        {
            isr = new InputStreamReader(new FileInputStream(fileName), "US-ASCII");
            in = new BufferedReader(isr);
        }
        catch (IOException ioException)
        {
            System.err.println("Error Opening File");
            isr = null;
            in = null;
        }

    }

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
        if (ch == -1) {
            return (EOI);
        } else if ((char) ch == NEWLINE) {
            currentlineNum++;
            return (NEWLINE);
        } else {
            return ((char)ch);
        }
    }

    public int getCurrentlineNum ()
    {
        return currentlineNum;
    }
    
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
