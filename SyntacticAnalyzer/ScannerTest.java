package miniJava.SyntacticAnalyzer;

import java.io.*;
import java.util.*;

public class ScannerTest 
{

    Scanner scanner;

    public ScannerTest(String file) 
    {

        scanner = new Scanner(file);
    }

    public void getTokens()
    {
        Token token = null;

        while (true) 
        {
            token = scanner.scanToken();
            System.out.println(token.toString());
            if (token.getKind() == Keywords.EOT)
                break;
        }
    
    }

    public static void main(String args[])
    {

        ScannerTest test = new ScannerTest(args[0]); 
        test.getTokens();

    }



}
