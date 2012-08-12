package api;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import system.Message;
import system.MessageQ;
import system.VertexImpl;

/**
 *
 * @author Pete Cappello
 */
public interface Vertex<VertexIdType, VertexValueType, EdgeValueType, MessageValueType>
{
    void addEdge( VertexIdType target, EdgeValueType edgeValue );

    void addEdge( VertexIdType vertexId, Object target, EdgeValueType edgeValue );

    void addVertex( VertexIdType vertexId, Object vertexValue );

    void aggregateOutputProblemAggregator( Aggregator aggregator );

    void aggregateOutputStepAggregator( Aggregator aggregator );
    /*
     * This method implements the algorithm used to solve the graph problem.
     */
    void compute();
    
    /**
     * @return true:  The in-degree of this vertex == 0 <br />
     *         false: The in-degree of this vertex > 0.
     */
    boolean isSource();

    String output();
    
    // TODO: VertexImpl: Omit this method to disallow applications from modifying the edgeMap.
    //       Replace with 1. Collection<VertexIdType> getEdgeTargetIds()
    //                    2. EdgeValueType getEdgeValue( VertexIdType targetId )
    Map<VertexIdType, EdgeValueType> getEdgeMap();
    
    Aggregator getInputStepAggregator();
    
    Aggregator getInputProblemAggregator();

    Iterator<Message<VertexIdType, MessageValueType>> getMessageIterator();
    
    MessageQ<VertexIdType, MessageValueType> getMessageQ();
    
    int getNumVertices();
    
    Collection<VertexIdType> getEdgeTargets();
    
    int getEdgeMapSize();
    
    int getPartId( VertexIdType vertexId, int numParts );
    
    long getSuperStep();
    
    /**
     * 
     * @return the vertex identifier, which is presumed to be unique for each vertex
     */
    VertexIdType getVertexId();
        
    VertexValueType getVertexValue();
    
    VertexImpl make( String line );
    
    void removeEdge( VertexIdType vertexId );

    void removeEdge( VertexIdType vertexId, Object targetVertexId );
    
    void removeVertex();
    
    void removeVertex( VertexIdType vertexId );

    void sendMessage( Object targetVertexId, Message message );
    
    void setVertexValue( VertexValueType vertexValue );
    
}
