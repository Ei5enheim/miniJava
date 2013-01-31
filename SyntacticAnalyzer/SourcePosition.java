/*
 * File: SourcePosition.java
 * Author: Rajesh Gopidi
 * PID:    720367703
 * Course : COMP520
 */

package miniJava.SyntacticAnalyzer;
/*
 * Class SourcePosition
 *
 * Objects of this class hold the position details with 
 * inthe source file of a particular token they are 
 * associated with.
 */
public class SourcePosition 
{
    private int start;
    private int finish;

    public SourcePosition()
    {
    
    }
    
    public SourcePosition( int start)
    {
        this.start = start;
    }

    public void setFinish(int finish)
    {
        this.finish = finish;
    }

    public String getPosition() 
    {
        return (start + "," + finish);
    }

}
