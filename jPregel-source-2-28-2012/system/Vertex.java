package system;

import static java.lang.System.out;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Implementation of topology mutations:
 * - Refactor code to work with stepToInboxMap instead of superstepToMessageQMap.
 * - Put AddEdge messageQ in Inbox.
 * - Design and implement Vertex AddEdge conflict "handler". Use combiner concept. where feasible.
 * - Put RemoveEdge messageQ in Inbox.
 * - Design and implement Vertex RemoveEdge conflict "handler". Use combiner concept, where feasible.
 * - Put AddVertex messageQ in Inbox. This is tricky, since the Vertex typically does not exist. If it does, it is a conflict. 
 *    Have Part add 1st, and let that Vertex resolve subsequent conflicts?
 * - Design and implement Vertex AddVertex conflict "handler". Use combiner concept. where feasible.
 * - Put RemoveVertex message in Inbox. Use combiners to resolve multiple requests. 
 *   How to handle request where no such Vertex exisits?
 * - Design and implement Vertex AddVertex conflict "handler". Use combiner concept, where feasible.
 * 
 * !! Is it safe & faster to make MessageQ thread-safe & remove synchronization of receive methods?
 * !! Should I make superStep Long instead of AtomicLong?
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
//    static public  Vertex make( String line, Combiner combiner ) { return null; }   
            
    private final Object vertexId;
    private final Combiner combiner;
    private transient Part part;
    
    // vertex state
    private Object vertexValue;
    private Map<Object, OutEdgeType> outEdgeMap;
    private NonNullMap<MessageValueType> superstepToMessageQMap;
    private NonNullMap<MessageValueType> superstepToInboxMap;
    private boolean currentStepIsActive = true;
    private boolean nextStepIsActive = true;
    private long superStep = -1L;
    
    private Aggregator outputStepAggregator;
    private Aggregator outputProblemAggregator;
    private ComputeInput computeInput;
    
    // coordination
    private int numMessagesSent;
    
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

    abstract protected void compute();

    abstract public String output();
    
    protected Aggregator getInputStepAggregator()    { return computeInput.getStepAggregator();    }
    
    protected Aggregator getInputProblemAggregator() { return computeInput.getProblemAggregator(); }

    synchronized protected Iterator<Message<MessageValueType>> getMessageIterator()
    {
        MessageQ<MessageValueType> messageQ = superstepToMessageQMap.remove( superStep );
        if ( messageQ == null )
        {
            messageQ = new MessageQ<MessageValueType>( combiner );
        }
        return messageQ.iterator(); 
    }
    
    synchronized protected MessageQ<MessageValueType> getMessageQ()
    {
        MessageQ<MessageValueType> messageQ = superstepToMessageQMap.remove( superStep );
        if ( messageQ == null )
        {
            messageQ = new MessageQ<MessageValueType>( combiner );
        }
        return messageQ; 
    }
    
    protected int getNumVertices() { return computeInput.getNumVertices(); }
    
    synchronized protected Collection<OutEdgeType> getOutEdgeValues() { return outEdgeMap.values(); }
    
    synchronized public int getOutEdgeMapSize() { return outEdgeMap.size(); }
    
    public int getPartId( int partitionSize ) { return vertexId.hashCode() % partitionSize; }
    
    synchronized protected long getSuperStep() { return superStep; }
    
    public Object getVertexId() { return vertexId; }
        
    public Object getVertexValue() { return vertexValue; }
    
    public abstract Vertex make( String line, Combiner combiner );
    
    synchronized protected OutEdgeType removeEdge( Object vertexId ) { return outEdgeMap.remove( vertexId ); }

    synchronized protected void removeEdge( Object vertexId, Object targetVertexId ) { }
    
    synchronized protected void removeVertex() { part.removeVertex( vertexId ); }
    
    synchronized protected void removeVertex( Object vertexId ) { part.removeVertex( vertexId ); }

    protected void sendMessage( Object targetVertexId, Message message )
    { 
        numMessagesSent++;
        part.sendMessage( targetVertexId, message, superStep + 1 ); 
    }
    
    protected void setOutputStepAggregator( Aggregator outputStepAggregator ) { this.outputStepAggregator = outputStepAggregator; }
    
    protected void setOutputProblemAggregator( Aggregator outputProblemAggregator ) { this.outputProblemAggregator = outputProblemAggregator; }
    
    protected void setVertexValue( Object vertexValue ) { this.vertexValue = vertexValue; }
    
    /* vertex deactivates itself by voting to halt.
     * vertex is activated only if it receives a message, in which case it
     * must explicitly deactivate, when it again wishes to halt.
     */
    synchronized protected void voteToHalt()
    { 
        MessageQ messageQ = superstepToMessageQMap.get( superStep + 1 );
        if ( messageQ.isEmpty() )
        {
            nextStepIsActive = false;
        }
    }   
    /* _________________________________________
     * 
     *               End API
     * _________________________________________
     */
    
    synchronized void advanceStep()
    {  
        superStep++;
        MessageQ messageQ = superstepToMessageQMap.get( superStep );
        if ( ! messageQ.isEmpty() )
        {
            nextStepIsActive = true;
        }
        currentStepIsActive = nextStepIsActive;
        numMessagesSent = 0;
    }
    
    synchronized int getNumMessagesSent() { return numMessagesSent; }

    synchronized Aggregator getOutputProblemAggregator() { return outputProblemAggregator; }
    
    synchronized Aggregator getOutputStepAggregator() { return outputStepAggregator; }
    
    synchronized boolean isActive() { return currentStepIsActive; }
    
    synchronized public boolean isNextStepActive() { return nextStepIsActive; }
    
    void receiveMessage( Message newMessage, long superStep ) { superstepToMessageQMap.get( superStep ).add( newMessage ); }
    
    void receiveMessageQ( MessageQ newMessageQ, long superStep ) { superstepToMessageQMap.get( superStep ).addAll( newMessageQ ); }
    
    void removeMessageQ( long superStep ) { superstepToMessageQMap.remove( superStep ); }       
    
    void setInput( ComputeInput computeInput ) { this.computeInput = computeInput; }  
                
    void setPart( Part part ) { this.part = part; }
}
