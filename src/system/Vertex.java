package system;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

// TODO  Implement topology mutations:
// * - Refactor code to work with stepToInboxMap instead of superstepToMessageQMap.
// * - Put AddEdge messageQ in Inbox.
// * - Design and implement Vertex AddEdge conflict "handler". Use combiner concept, where feasible.
// * - Put RemoveEdge messageQ in Inbox.
// * - Design and implement Vertex RemoveEdge conflict "handler". Use combiner concept, where feasible.
// * - Put AddVertex messageQ in Inbox. This is tricky, since the Vertex typically does not exist. If it does, it is a conflict. 
// *    Have Part add 1st, and let that Vertex resolve subsequent conflicts?
// * - Design and implement Vertex AddVertex conflict "handler". Use combiner concept. where feasible.
// * - Put RemoveVertex message in Inbox. Use combiners to resolve multiple requests. 
// *   How to handle request where no such Vertex exists?
// * - Design and implement Vertex AddVertex conflict "handler". Use combiner concept, where feasible.
// 
/**
 * !! Is it safe & faster to make MessageQ thread-safe & remove synchronization of receive methods?
 * 
 * I currently think that vertex does not need the bit of state designating it 
 * active/inactive. Instead I:
 * 
 *  1. make it the responsibility of the graph maker to add source vertices in 
 *     the active set for the initial super step;
 *  2. thereafter regard a vertex as active during superStep s if and only if 
 *     its MessageQ for superStep s is nonempty.
 * 
 * The opinion assumes that a vertex has no basis for activity unless it 
 * receives a message; otherwise nothing has changed since it last sent messages 
 * to other vertices. This assumption implies that changing the superStep does 
 * not itself constitute a state change for the vertex. If I encounter an 
 * algorithm that falsifies this assumption, I will revise this view. 
 * 
 * In the meantime, the compute method no longer needs to vote to halt.
 * This method thus has been removed from the API.
 * 
 * When a vertex completes the compute method for a super step, it may not have
 * received all its messages for the next super step. When the compute method
 * for the next super step is invoked, it has all its messages for that step.
 * Messages that arrive after the compute method for super step n completes, but 
 * before the compute method for step n + 1 begins may be for either super step 
 * n + 1 or super step n + 2. The vertex cannot determine which, without 
 * additional information. Thus, each message is associated with a super step 
 * number.
 *
 * @author Peter Cappello
 */
abstract public class Vertex<VertexIdType, VertexValueType, EdgeValueType, MessageValueType> implements java.io.Serializable
{
    public    static Combiner combiner; // null means no combining
    protected static int      numVertices;
    
    private final VertexIdType    vertexId;
    private       VertexValueType vertexValue;
    private       Part            part;
    
    private Map<VertexIdType, EdgeValueType> edgeMap;
//    private NonNullMap<VertexIdType, MessageValueType> superstepToMessageQMap;
    private OntoMap<MessageQ<VertexIdType, MessageValueType>> superstepToMessageQMap;
//    private OntoMap<MessageValueType> superstepToInboxMap;
            
    public Vertex() { vertexId = null; }
          
    public Vertex( VertexIdType vertexId, Map<VertexIdType, EdgeValueType> edgeMap )
    {
        this.vertexId   = vertexId;
        this.edgeMap = edgeMap;
//        superstepToMessageQMap = new NonNullMap<VertexIdType, MessageValueType>( combiner );
        superstepToMessageQMap = new OntoMap<MessageQ<VertexIdType, MessageValueType>>( new MessageQ( combiner ) );
    }
    
    /* _________________________________________
     * 
     *               Begin API
     * _________________________________________
     */   
    synchronized protected void addEdge( VertexIdType target, EdgeValueType edgeValue ) { edgeMap.put( target, edgeValue ); }

    synchronized protected void addEdge( VertexIdType vertexId, Object target, EdgeValueType edgeValue ) { }

    synchronized protected void addVertex( VertexIdType vertexId, Object vertexValue ) { /* combiner */ }

    protected void aggregateOutputProblemAggregator( Aggregator aggregator ) { part.aggregateOutputProblemAggregator( aggregator ); }

    protected void aggregateOutputStepAggregator( Aggregator aggregator ) { part.aggregateOutputStepAggregator( aggregator ); }
    
    abstract protected void compute();
    
    abstract protected boolean isSource();

    abstract public String output();
    
    protected Map<VertexIdType, EdgeValueType> getEdgeMap() { return edgeMap; }
    
    protected Aggregator getInputStepAggregator()    { return part.getComputeInput().getStepAggregator();    }
    
    protected Aggregator getInputProblemAggregator() { return part.getComputeInput().getProblemAggregator(); }

    synchronized protected Iterator<Message<VertexIdType, MessageValueType>> getMessageIterator()
    {
        MessageQ<VertexIdType, MessageValueType> messageQ = superstepToMessageQMap.remove( getSuperStep() );
        if ( messageQ == null )
        {
            messageQ = new MessageQ<VertexIdType, MessageValueType>( combiner );
        }
        return messageQ.iterator(); 
    }
    
    synchronized protected MessageQ<VertexIdType, MessageValueType> getMessageQ()
    {
        MessageQ<VertexIdType, MessageValueType> messageQ = superstepToMessageQMap.remove( getSuperStep() );
        if ( messageQ == null )
        {
            messageQ = new MessageQ<VertexIdType, MessageValueType>( combiner );
        }
        return messageQ; 
    }
    
    protected int getNumVertices() { return part.getComputeInput().getNumVertices(); }
    
    synchronized protected Collection<VertexIdType> getEdgeTargets() { return edgeMap.keySet(); }
    
    synchronized public int getEdgeMapSize() { return edgeMap.size(); }
    
    public int getPartId( VertexIdType vertexId, int numParts ) 
    { 
        return vertexId.hashCode() % numParts; 
    }
    
    synchronized protected long getSuperStep() { return part.getSuperStep(); }
    
    public VertexIdType getVertexId() { return vertexId; }
        
    public VertexValueType getVertexValue() { return vertexValue; }
    
    public abstract Vertex make( String line );
    
    synchronized protected void removeEdge( VertexIdType vertexId ) { edgeMap.remove( vertexId ); }

    synchronized protected void removeEdge( VertexIdType vertexId, Object targetVertexId ) { }
    
    synchronized protected void removeVertex() { part.removeVertex( vertexId ); }
    
    synchronized protected void removeVertex( VertexIdType vertexId ) { part.removeVertex( vertexId ); }

    protected void sendMessage( Object targetVertexId, Message message )
    {
        part.sendMessage( targetVertexId, message, getSuperStep() + 1 ); 
    }
        
    protected void setVertexValue( VertexValueType vertexValue ) { this.vertexValue = vertexValue; }
    
    /* vertex deactivates itself by voting to halt.
     * vertex is activated only if it receives a message, in which case it
     * must explicitly deactivate, when it again wishes to halt.
     */
//    synchronized protected void voteToHalt()
//    { 
//        MessageQ messageQ = superstepToMessageQMap.get( getSuperStep() + 1 );
//        if ( messageQ.isEmpty() )
//        {
//            nextStepIsActive = false;
//        }
//    }   
    /* _________________________________________
     * 
     *               End API
     * _________________________________________
     */
    
//    synchronized public boolean isNextStepActive() { return nextStepIsActive; }
    
    Combiner getCombiner() { return combiner; }
    
    void receiveMessage( Message newMessage, long superStep )
    { 
        superstepToMessageQMap.get( superStep ).add( newMessage );
    }
    
    void receiveMessageQ( MessageQ newMessageQ, long superStep ) { superstepToMessageQMap.get( superStep ).addAll( newMessageQ ); }
        
    void removeMessageQ( long superStep ) { superstepToMessageQMap.remove( superStep ); }
                    
    void setPart( Part part ) { this.part = part; }
    
    void setNumVertices( int numV ) { numVertices = numV; }
}
