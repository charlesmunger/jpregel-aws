package api;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import edu.ucsb.jpregel.system.Message;
import edu.ucsb.jpregel.system.MessageQ;
import edu.ucsb.jpregel.system.VertexImpl;

/**
 * Graphs are comprised of Vertex objects, which have several type parameters.
 * <ol>
 *   <li><b>VertexIdType</b> - the type of the vertex identifier. For example, in a 
 *       graph embedded in the 2D plane, each vertex may be identified by its 
 *       coordinates in the 2D plane: 
 *       a Point2D.Float may serve as the vertex identifier.
 *   </li>
 *   <li><b>VertexValueType</b> - the type of the value associated with a vertex.
 *       For example, in a single source shortest path problem, we may wish to 
 *       associate 2 items with each vertex <i>v</i>:
 *       <ul>
 *         <li>the shortest path to from the source vertex to <i>v</i>;</li>
 *         <li>
 *           the vertex identifier of the vertex that immediately precedes 
 *           <i>v</i> on a shortest path from the source to <i>v</i>.
 *         </li>
 *       </ul>
 *   </li>
 *   <li><b>EdgeValueType</b> - the type of the value associated with an edge.</li>
 *   <li><b>MessageValueType</b> - The type of the value associated with a message.
 *     Messages are sent from 1 vertex to another.
 *     A message has 2 attributes: 
 *       1) the vertexId of the sending vertex, and
 *       2) the <i>value</i> of the message: the message itself.
 *   </li>
 * </ol>
 *
 * @author Pete Cappello
 */
public interface Vertex<VertexIdType, VertexValueType, EdgeValueType, MessageValueType>
{
    /**
     * Add an edge to this vertex.
     * 
     * @param target the vertex identifier of the endpoint vertex.
     * @param edgeValue the value of the edge from this vertex to the target.
     */
    void addEdge( VertexIdType target, EdgeValueType edgeValue );

    /**
     * Add an edge from <i>sourceVertexId</i> to <i>targetVertexId</i> with a value of <i>edgeValue</i>.
     * To be implemented.
     * 
     * @param sourceVertexId the vertex identifier of the originating vertex.
     * @param targetVertexId the vertex identifier of the endpoint vertex.
     * @param edgeValue the value of the edge from the sourceVertex to the target.
     */
    void addEdge( VertexIdType sourceVertexId, VertexIdType targetVertexId, EdgeValueType edgeValue );

    /**
     * Add a vertex to the graph.
     * 
     * @param vertexId the vertex identifier of the vertex to be added.
     * @param vertexValue the vertex value of the vertex to be added.
     */
    void addVertex( VertexIdType vertexId, Object vertexValue );

    /**
     * 
     * @param aggregator 
     */
    void aggregateOutputProblemAggregator( Aggregator aggregator );

    void aggregateOutputStepAggregator( Aggregator aggregator );
    
    /**
     * This method implements the algorithm used to solve the graph problem.
     */
    void compute();
    
    /**
     * Intention: Return true if and only if this vertex is in the initial superStep's activeSet.
     * Is invoked when vertex is added to part.
     * 
     * @return true: if and only if this vertex is in the initial superStep's activeSet.<br />
     */
    boolean isInitiallyActive();

    /**
     * 
     * @return 
     */
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
    
    /**
     * Get the current super step value.
     * This value is defined only within the execution scope of the compute method.
     * 
     * @return the value of the current super step.
     */
    long getSuperStep();
    
    /**
     * Get the vertex identifier of this vertex.
     * 
     * @return the vertex identifier, which is presumed to be unique for each vertex.
     */
    VertexIdType getVertexId();
        
    /**
     * Get the value associated with this vertex.
     * 
     * @return the value associated with this vertex.
     */
    VertexValueType getVertexValue();
    
//    /**
//     * Get the number of the Worker to which this part is assigned.
//     * 
//     * @param partId 
//     * @param numWorkers
//     * @return Worker number (>= 1)
//     */
//    int getWorkerNum( int partId, int numWorkers );
    
    /**
     * Make a vertex from its String representation.
     * 
     * @param stringVertex the String representation of the vertex.
     * @return a VertexImpl object corresponding to the String representation, stringVertex.
     */
    VertexImpl make( String stringVertex );
    
    void removeEdge( VertexIdType vertexId );

    void removeEdge( VertexIdType vertexId, Object targetVertexId );
    
    void removeVertex();
    
    void removeVertex( VertexIdType vertexId );

    /**
     * Send a message to the vertex whose identifier is <i>targetVertexId</i>.
     * 
     * @param targetVertexId
     * @param message 
     */
    void sendMessage( Object targetVertexId, Message message );
    
    /**
     * Set the value of this vertex.
     * 
     * @param vertexValue the value to be set.
     */
    void setVertexValue( VertexValueType vertexValue );
    
}
