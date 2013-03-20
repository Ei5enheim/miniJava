package miniJava;

import java.io.*;
//import java.util.*;
import miniJava.SyntacticAnalyzer.*;
import miniJava.AbstractSyntaxTrees.*;
import miniJava.ContextualAnalyzer.*;

public class Compiler 
{
    miniJava.AbstractSyntaxTrees.Package pac;
    Scanner scanner;
    Parser parser;

    public Compiler(String file) 
    {

        scanner = new Scanner(file);
        parser = new Parser(scanner);
    }

    public static void main(String args[])
    {
        miniJava.AbstractSyntaxTrees.Package pac = null;
        ASTDisplay astDisplay = new ASTDisplay ();
        Compiler test = new Compiler(args[0]); 
        pac = (miniJava.AbstractSyntaxTrees.Package) test.parser.parseFile(); 
        //astDisplay.showTree(pac);
        Identification identifier = new Identification();
        Checker typeChecking = new Checker();
        System.out.println("doing Identification");
        identifier.Identify(pac);
        typeChecking.typeCheck(pac);
        System.out.println("successfully done");
        System.exit(0);
    }
}

