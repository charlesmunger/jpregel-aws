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
// * 
/**
 * !! Is it safe & faster to make MessageQ thread-safe & remove synchronization of receive methods?
 * 
 * I currently am of the opinion that vertex does not need the bit of state 
 * explicitly designating it active. Instead I will:
 * 
 *  1. make it the responsibility of the graph maker to add source vertices in 
 *     the active set for the initial super step;
 *  2. thereafter regard a vertex as active during superStep s if and only if 
 *     its MessageQ for superStep s is nonempty.
 * 
 * The opinion assumes that a vertex has no basis for activity unless it 
 * receives a message; otherwise nothing has changed since it last sent messages 
 * to other vertices. This assumption implies that changing the superStep does 
 * not in and of itself constitute a state change for the vertex. If I encounter 
 * an algorithm that falsifies this assumption, I will revise this view. 
 * 
 * In the meantime, a vertex'x compute method no longer needs to vote to halt.
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
abstract public class Vertex<OutEdgeType, MessageValueType> implements java.io.Serializable
{
    // TODO vertexID: Use generics to avoid casts
    private final Object vertexId;
    
    // TODO: Vertex: Eliminate Combiner field; get from Part 
    private final Combiner combiner;
    private Part part;
    
    // vertex state
    private Object vertexValue;
    private Map<Object, OutEdgeType> outEdgeMap;
    private NonNullMap<MessageValueType> superstepToMessageQMap;
    private NonNullMap<MessageValueType> superstepToInboxMap;
            
    public Vertex()
    {
        vertexId = null;
        combiner = null;
    }
                      
    public Vertex( Object vertexId, Map<Object, OutEdgeType> outEdgeMap, Combiner combiner )
    {
        this.vertexId   = vertexId;
        this.outEdgeMap = outEdgeMap;
        this.combiner   = combiner;
        superstepToMessageQMap = new NonNullMap<MessageValueType>( combiner );
    }
    
    /* _________________________________________
     * 
     *               Begin API
     * _________________________________________
     */   
    synchronized protected void addEdge( Object target, OutEdgeType outEdge ) { outEdgeMap.put( target, outEdge ); }

    synchronized protected void addEdge( Object vertexId, Object target, OutEdgeType outEdge ) { }

    synchronized protected void addVertex( Object vertexId, Object vertexValue ) { /* combiner */ }

    protected void aggregateOutputProblemAggregator( Aggregator aggregator ) { part.aggregateOutputProblemAggregator( aggregator ); }

    protected void aggregateOutputStepAggregator( Aggregator aggregator ) { part.aggregateOutputStepAggregator( aggregator ); }
    
    abstract protected void compute();
    
    abstract protected boolean isSource();

    abstract public String output();
    
    protected Aggregator getInputStepAggregator()    { return part.getComputeInput().getStepAggregator();    }
    
    protected Aggregator getInputProblemAggregator() { return part.getComputeInput().getProblemAggregator(); }

    synchronized protected Iterator<Message<MessageValueType>> getMessageIterator()
    {
        MessageQ<MessageValueType> messageQ = superstepToMessageQMap.remove( getSuperStep() );
        if ( messageQ == null )
        {
            messageQ = new MessageQ<MessageValueType>( combiner );
        }
        return messageQ.iterator(); 
    }
    
    synchronized protected MessageQ<MessageValueType> getMessageQ()
    {
        MessageQ<MessageValueType> messageQ = superstepToMessageQMap.remove( getSuperStep() );
        if ( messageQ == null )
        {
            messageQ = new MessageQ<MessageValueType>( combiner );
        }
        return messageQ; 
    }
    
    protected int getNumVertices() { return part.getComputeInput().getNumVertices(); }
    
    synchronized protected Collection<OutEdgeType> getOutEdgeValues() { return outEdgeMap.values(); }
    
    synchronized public int getOutEdgeMapSize() { return outEdgeMap.size(); }
    
    public int getPartId( Object vertexId, int numParts ) 
    { 
        return vertexId.hashCode() % numParts; 
    }
    
    synchronized protected long getSuperStep() { return part.getSuperStep(); }
    
    public Object getVertexId() { return vertexId; }
        
    public Object getVertexValue() { return vertexValue; }
    
    public abstract Vertex make( String line, Combiner combiner );
    
    synchronized protected OutEdgeType removeEdge( Object vertexId ) { return outEdgeMap.remove( vertexId ); }

    synchronized protected void removeEdge( Object vertexId, Object targetVertexId ) { }
    
    synchronized protected void removeVertex() { part.removeVertex( vertexId ); }
    
    synchronized protected void removeVertex( Object vertexId ) { part.removeVertex( vertexId ); }

    protected void sendMessage( Object targetVertexId, Message message )
    {
        part.sendMessage( targetVertexId, message, getSuperStep() + 1 ); 
    }
    
//    protected void setOutputStepAggregator( Aggregator outputStepAggregator ) { this.outputStepAggregator = outputStepAggregator; }
//    
//    protected void setOutputProblemAggregator( Aggregator outputProblemAggregator ) { this.outputProblemAggregator = outputProblemAggregator; }
    
    protected void setVertexValue( Object vertexValue ) { this.vertexValue = vertexValue; }
    
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
    
    void receiveMessage( Message newMessage, long superStep )
    { 
        superstepToMessageQMap.get( superStep ).add( newMessage );
    }
    
    void receiveMessageQ( MessageQ newMessageQ, long superStep ) { superstepToMessageQMap.get( superStep ).addAll( newMessageQ ); }
        
    void removeMessageQ( long superStep ) { superstepToMessageQMap.remove( superStep ); }
                    
    void setPart( Part part ) { this.part = part; }
}
