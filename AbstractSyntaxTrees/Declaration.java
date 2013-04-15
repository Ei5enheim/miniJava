/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;
import miniJava.CodeGenerator.FieldAddress;

public abstract class Declaration extends AST {
	
	public Declaration(String name, Type type, SourcePosition posn) {
		super(posn);
		this.name = name;
		this.type = type;
		storage = new FieldAddress();
	}
	
	public String name;
	public Type type;
	public FieldAddress storage;
}
