package system;

/**
 *
 * @author Peter Cappello
 */
public class OutEdge
{
    private int targetVertexId;
    private int edgeValue;
    
    public OutEdge( int targetVertexId, int edgeValue )
    {
        this.targetVertexId = targetVertexId;
        this.edgeValue      = edgeValue;
    }
    
    public OutEdge(int targetVertexId)
    { 
        this.targetVertexId = targetVertexId;

    }
    public int  getVertexId()  { return targetVertexId; }
    
    public int  getEdgeValue() { return edgeValue; }
    
    public void setEdgeValue( double vertexValue ) { this.edgeValue = edgeValue; }
}
