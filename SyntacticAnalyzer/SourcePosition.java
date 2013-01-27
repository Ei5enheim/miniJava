package miniJava.SyntacticAnalyzer;
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
