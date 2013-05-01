/*
 * File:    SymbolTable.java
 * Author:  Rajesh Gopidi
 * PID:     720367703
 * Course : COMP520
 */

package miniJava.ContextualAnalyzer;

import java.util.*;
import miniJava.ContextualAnalyzer.STEntry;
import miniJava.AbstractSyntaxTrees.Declaration;
public class SymbolTable 
{

    protected ArrayList<HashMap<String, STEntry>> symbolTable;
    protected int level;
    protected int parentScope;
    private static final int symbolTableSize = 20; 
    private boolean debug  = false;

    public SymbolTable()
    {
        parentScope = -1;
        level = -1;
        symbolTable = new ArrayList<HashMap<String, STEntry>>(symbolTableSize);
    }

    public void newScope()
    {
        HashMap<String, STEntry> innerTable = new HashMap<String, STEntry> (symbolTableSize);
        level++;
        if (debug)
            System.out.println("new Scope level: " + level);

        symbolTable.add(innerTable);
    }

    public void newScope(boolean setParentScope)
    {
        HashMap<String, STEntry> innerTable = new HashMap<String, STEntry> (symbolTableSize);

	level++;

        if (setParentScope)
            parentScope = level;

        if (debug)
            System.out.println("new Scope level: " + level + "parent scope level" + parentScope);

        symbolTable.add(innerTable);
    }

    public void closeScope()
    {
        symbolTable.remove(level);
        level--;
        if (debug)
            System.out.println("back to Scope level: " + level + ", parent scope level" + parentScope);
    }

    public void closeScope(boolean resetParentScope)
    {
        symbolTable.remove(level);
        level--;
        if (resetParentScope && level < MEMBERLEVEL)
            parentScope = -1;
	else if (resetParentScope)
	    parentScope = level;
        if (debug)
            System.out.println("back to Scope level: " + level + ", parent scope level" + parentScope);
    }

    public boolean add (String key, Declaration decl)
    {
        HashMap<String, STEntry> innerTable = symbolTable.get(level);
        HashMap<String, STEntry> parentTable = null; 
        STEntry entry = null;
        
        if (innerTable.containsKey(key)) {
            return (true);
        }

        if (level > parentScope && (parentScope != -1)) {
            int index = level-1;
            while (index >= parentScope) {
                parentTable = symbolTable.get(index);
                if (parentTable.containsKey(key))
                    return (true);
                index--;
            }
	}

        entry = new STEntry(decl, null);
        innerTable.put(key, entry);

        return (false);
    }

    public Declaration retrieve (String key)
    {
        int index = symbolTable.size() - 1;
        HashMap<String, STEntry> innerTable = null;        

        while (index >= 0) {
            innerTable = symbolTable.get(index);
            if (innerTable.containsKey(key)) {
                return (innerTable.get(key).astNode);
            } else {
                index--;
            }
        }
        return (null);
    }

    public Declaration retrieveClassDecl (String key)
    {
        HashMap<String, STEntry> innerTable = null;        

       innerTable = symbolTable.get(CLASSLEVEL);

       if (innerTable.containsKey(key)) {
           return (innerTable.get(key).astNode);
       }
 
       innerTable = symbolTable.get(PREDEFINEDLEVEL);

       if (innerTable.containsKey(key)) {
	   return (innerTable.get(key).astNode);
       }
       return (null);
    }

    public Declaration retrievePDefClassDecl (String key)
    {
        HashMap<String, STEntry> innerTable = null;        

       innerTable = symbolTable.get(PREDEFINEDLEVEL);

       if (innerTable.containsKey(key)) {
           return (innerTable.get(key).astNode);
       }
       return (null);
    }


    public Declaration retrieveMemberDecl (String key)
    {
        int index = MEMBERLEVEL;
        HashMap<String, STEntry> innerTable = null;        

       innerTable = symbolTable.get(index);
       if (innerTable.containsKey(key)) {
           return (innerTable.get(key).astNode);
       } 
       return (null);
    }

    public int retrieveLevel (String key)
    {
        int index = symbolTable.size() - 1;
        HashMap<String, STEntry> innerTable = null;

        while (index >= 0) {
            innerTable = symbolTable.get(index);
            if (innerTable.containsKey(key)) {
                return (index);
            } else {
                index--;
            }
        }
        return (-1);
    }
    
    public static final int CLASSLEVEL = 1;
    public static final int PREDEFINEDLEVEL = 0;
    public static final int MEMBERLEVEL = 2;
}



