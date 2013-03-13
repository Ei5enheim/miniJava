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
    private boolean debug  = true;

    public SymbolTable()
    {
        parentScope = -1;
        // level = 0 is reserved for keywords
        level = 1;
        symbolTable = new ArrayList<HashMap<String, STEntry>>(symbolTableSize);
    }

    public void newScope()
    {
        HashMap<String, STEntry> innerTable = new HashMap<String, STEntry> (symbolTableSize);
        level++;
        if (debug)
            System.out.println("new Scope level: " + level);

        symbolTable.add(Integer.valueOf(level), innerTable);
    }

    public void newScope(boolean setParentScope)
    {
        HashMap<String, STEntry> innerTable = new HashMap<String, STEntry> (symbolTableSize);

        if (setParentScope)
            parentScope = level;

        level++;

        if (debug)
            System.out.println("new Scope level: " + level);

        symbolTable.add(innerTable);
    }

    public void closeScope()
    {
        symbolTable.remove(level);
        level--;
    }

    public void closeScope(boolean resetParentScope)
    {
        symbolTable.remove(level);
        level--;
        if (resetParentScope)
            parentScope = level;
    }

    public boolean add (String key, Declaration decl)
    {
        HashMap<String, STEntry> innerTable = symbolTable.get(level);
        HashMap<String, STEntry> parentTable = null; 
        HashMap<String, STEntry> keywords = symbolTable.get(0);
        STEntry entry = null;

        if (keywords.containsKey(key)) {
            // need to change this and add a Error and return
            return (true);
        }
        if (parentScope > 1)
            parentTable = symbolTable.get(parentScope);

        if (innerTable.containsKey(key) ||
            parentTable.containsKey(key) || 
            defaults.containsKey(key)) {
            return (true);
        } 

        entry = new STEntry(decl, null);
        innerTable.put(key, entry);

        return (false);
    }

    public Declaration retrieve (String key)
    {
        int index = symbolTable.size() - 1;
        STEntry entry = null;
        HashMap<String, STEntry> innerTable = null;        

        while (index > 0) {
            innerTable = symbolTable.get(index);
            if (innerTable.containsKey(key)) {
                return (innerTable.get(key).astNode);
            } else {
                index--;
            }
        }
        return (null);
    }
}

