package miniJava;

import java.io.*;
//import java.util.*;
import miniJava.SyntacticAnalyzer.*;
import miniJava.AbstractSyntaxTrees.*;
import miniJava.ContextualAnalyzer.*;
import miniJava.CodeGenerator.*;
import mJAM.*;

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
        System.out.println("Initiating Identification");
        identifier.Identify(pac);
        System.out.println("Initiating Type checking");
        typeChecking.typeCheck(pac);
        CodeGenerator codeGenerator = new CodeGenerator();
        codeGenerator.generateCode(pac);
        String objectCodeFileName = new String(args[0].substring(0, args[0].lastIndexOf('.') + 1)+
                                                    "mJAM");
        ObjectFile objF = new ObjectFile(objectCodeFileName);
        System.out.println("Generating the code file " + objectCodeFileName + " ..:)");
        if (objF.write()) {
            System.out.println("FAILED to generate the code file!");
            return;
        } else {
            System.out.println("SUCCEEDED in generating the code file!");
        }
        
        // create the asm file
        System.out.println("writing assembly file ..");
        
        Disassembler d = new Disassembler(objectCodeFileName);
        if (d.disassemble()) {
            System.out.println("FAILED to generate the assembly file!");
            return;
        } else {
            System.out.println("SUCCEEDED in generating the assembly file!");
        }
        System.out.println("Running code ..");
        Interpreter.interpret(objectCodeFileName);
        
        System.out.println("Finished execution");
        System.exit(0);
    }
}

