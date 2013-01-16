package edu.ucsb.jpregel.system;

/**
 *
 * @author Peter Cappello
 */
public class OutEdge
{
    private Integer targetVertexId;
    private Integer edgeValue;
    
    public OutEdge( Integer targetVertexId, Integer edgeValue )
    {
        this.targetVertexId = targetVertexId;
        this.edgeValue      = edgeValue;
    }
    
    public OutEdge(Integer targetVertexId) { this.targetVertexId = targetVertexId; }
    
    public Integer  getVertexId()  { return targetVertexId; }
    
    public Integer  getEdgeValue() { return edgeValue; }
    
    public void setEdgeValue( Integer vertexValue ) { this.edgeValue = edgeValue; }
}
