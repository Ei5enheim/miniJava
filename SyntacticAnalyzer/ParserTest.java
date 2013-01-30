package miniJava.SyntacticAnalyzer;

import java.io.*;
import java.util.*;

public class ParserTest 
{

    Scanner scanner;
    Parser parser;

    public ParserTest(String file) 
    {

        scanner = new Scanner(file);
        parser = new Parser(scanner);
    }

    public static void main(String args[])
    {

        ParserTest test = new ParserTest(args[0]); 
        test.parser.parseFile(); 
    }
}

