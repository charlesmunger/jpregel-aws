package system;

import api.Aggregator;
import api.Vertex;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

// TODO  Implement topology mutations:
// * - Refactor code to work with stepToInboxMap instead of superstepToMessageQMap.
// * - Put AddEdge messageQ in Inbox.
// * - Design and implement VertexImpl AddEdge conflict "handler". Use combiner concept, where feasible.
// * - Put RemoveEdge messageQ in Inbox.
// * - Design and implement VertexImpl RemoveEdge conflict "handler". Use combiner concept, where feasible.
// * - Put AddVertex messageQ in Inbox. This is tricky, since the VertexImpl typically does not exist. If it does, it is a conflict. 
// *    Have Part add 1st, and let that VertexImpl resolve subsequent conflicts?
// * - Design and implement VertexImpl AddVertex conflict "handler". Use combiner concept. where feasible.
// * - Put RemoveVertex message in Inbox. Use combiners to resolve multiple requests. 
// *   How to handle request where no such VertexImpl exists?
// * - Design and implement VertexImpl AddVertex conflict "handler". Use combiner concept, where feasible.

// * TODO VertexImpl: ? Is it safe & faster to make MessageQ thread-safe & remove synchronization of receive methods?

/*
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
abstract public class VertexImpl<VertexIdType, VertexValueType, EdgeValueType, MessageValueType> 
implements Vertex<VertexIdType, VertexValueType, EdgeValueType, MessageValueType>, java.io.Serializable
{
    public    static Combiner combiner; // null means no combining
    protected static int      numVertices;
    
    private final VertexIdType    vertexId;
    private       VertexValueType vertexValue;
    private       Part            part;
    
    private Map<VertexIdType, EdgeValueType> edgeMap;
    private OntoMap<MessageQ<VertexIdType, MessageValueType>> superstepToMessageQMap;
//    private OntoMap<MessageValueType> superstepToInboxMap;
            
    public VertexImpl() { vertexId = null; }
          
    public VertexImpl( VertexIdType vertexId, Map<VertexIdType, EdgeValueType> edgeMap, int numOutgoingEdges)
    {
        this.vertexId   = vertexId;
        this.edgeMap = edgeMap;
        superstepToMessageQMap = new OntoMap<MessageQ<VertexIdType, MessageValueType>>(numOutgoingEdges, new MessageQ( combiner ) );
    }
    
    public VertexImpl( VertexIdType vertexId, Map<VertexIdType, EdgeValueType> edgeMap)
    {
        this(vertexId, edgeMap,100);
    }
    
    /* _________________________________________
     * 
     *               Begin API
     * _________________________________________
     */   
    @Override
    synchronized public void addEdge( VertexIdType target, EdgeValueType edgeValue ) { edgeMap.put( target, edgeValue ); }

    @Override
    synchronized public void addEdge( VertexIdType vertexId, VertexIdType target, EdgeValueType edgeValue ) { }

    @Override
    synchronized public void addVertex( VertexIdType vertexId, Object vertexValue ) { /* combiner */ }

    @Override
    public void aggregateOutputProblemAggregator( Aggregator aggregator ) { part.aggregateOutputProblemAggregator( aggregator ); }

    @Override
    public void aggregateOutputStepAggregator( Aggregator aggregator ) { part.aggregateOutputStepAggregator( aggregator ); }

    @Override
    abstract public void compute();
    
    @Override
    abstract public boolean isSource();

    @Override
    abstract public String output();
    
    // TODO: VertexImpl: Omit this method to disallow applications from modifying the edgeMap.
    //       Replace with 1. Collection<VertexIdType> getEdgeTargetIds()
    //                    2. EdgeValueType getEdgeValue( VertexIdType targetId )
    @Override
    public Map<VertexIdType, EdgeValueType> getEdgeMap() { return edgeMap; }
    
    @Override
    public Aggregator getInputStepAggregator()    { return part.getComputeInput().getStepAggregator();    }
    
    @Override
    public Aggregator getInputProblemAggregator() { return part.getComputeInput().getProblemAggregator(); }

    @Override
    synchronized public Iterator<Message<VertexIdType, MessageValueType>> getMessageIterator()
    {
        return getMessageQ().iterator();
    }
    
    @Override
    synchronized public MessageQ<VertexIdType, MessageValueType> getMessageQ()
    {
        MessageQ<VertexIdType, MessageValueType> messageQ = superstepToMessageQMap.remove( getSuperStep() );
        if ( messageQ == null )
        {
            messageQ = new MessageQ<VertexIdType, MessageValueType>( combiner );
        }
        return messageQ; 
    }
    
    @Override
    public int getNumVertices() { return part.getComputeInput().getNumVertices(); }
    
    @Override
    synchronized public Collection<VertexIdType> getEdgeTargets() { return edgeMap.keySet(); }
    
    @Override
    synchronized public int getEdgeMapSize() { return edgeMap.size(); }
    
    @Override
    public int getPartId( VertexIdType vertexId, int numParts ) 
    { 
        return vertexId.hashCode() % numParts; 
    }
    
    @Override
    synchronized public long getSuperStep() { return part.getSuperStep(); }
    
    @Override
    public VertexIdType getVertexId() { return vertexId; }
        
    @Override
    public VertexValueType getVertexValue() { return vertexValue; }
    
    @Override
    public abstract VertexImpl make( String line );
    
    @Override
    synchronized public void removeEdge( VertexIdType vertexId ) { edgeMap.remove( vertexId ); }

    @Override
    synchronized public void removeEdge( VertexIdType vertexId, Object targetVertexId ) { }
    
    @Override
    synchronized public void removeVertex() { part.removeVertex( vertexId ); }
    
    @Override
    synchronized public void removeVertex( VertexIdType vertexId ) { part.removeVertex( vertexId ); }

    @Override
    public void sendMessage( Object targetVertexId, Message message )
    {
        part.sendMessage( targetVertexId, message, getSuperStep() + 1 ); 
    }
        
    @Override
    public void setVertexValue( VertexValueType vertexValue ) { this.vertexValue = vertexValue; }
    
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
