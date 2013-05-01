/*
 * File: NullLiteral.java
 * Author: Rajesh Gopidi
 * PID:    720367703
 * Course : COMP520
 */

package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class NullLiteral extends Literal {

  public NullLiteral(SourcePosition posn) {
    super ("null",posn);
  }
 
  public <A,R> R visit(Visitor<A,R> v, A o) {
      return v.visitNullLiteral(this, o);
  }
}
