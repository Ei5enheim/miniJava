/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public abstract class Reference extends AST
{
	public Reference(SourcePosition posn){
		super(posn);
		this.id = null;	
		this.ref = null;
	}
	public Reference (Identifier id, SourcePosition posn)
	{
		super(posn);
		this.ref = null;
        	this.id = id;
	}

    	public void setRef(Reference ref) 
    	{
        	this.ref = ref;
    	}

    	public Identifier id;
    	public Reference ref;
}
