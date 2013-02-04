package edu.ucsb.jpregel.system;
 
import api.Aggregator;
import api.Vertex;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.infinispan.util.concurrent.jdk8backported.ConcurrentHashMapV8;

/**
 *
 * @author Peter Cappello
 */
public final class Part
{
    private final int partId;
    private final Job job;
    
    private Map<Object, VertexImpl> vertexIdToVertexMap = new ConcurrentHashMapV8<Object, VertexImpl>( 8000 , 0.9f, 2);
    private OntoMap<Set<VertexImpl>> superstepToActiveSetMap = new OntoMap<Set<VertexImpl>>( new ActiveSet() );
    
    // superStep parameters
    private ComputeThread computeThread;
    private long superStep;
    private ComputeInput computeInput;
    
    // superStep parameters modified by each vertex.compute()
    private Aggregator outputProblemAggregator;
    private Aggregator outputStepAggregator;
    private int numMessagesSent; 
    
    Part( int partId, Job job )
    {
        this.partId = partId;
        this.job    = job;
    }
    
    /*
     * FIXME: For graph mutation (add vertex), also need void addVertexToActiveSet( Long superStep, VertexImpl vertex )
     */
    void add( VertexImpl vertex )
    {
        vertex.setPart( this );
        vertexIdToVertexMap.put( vertex.getVertexId(), vertex );
        if ( vertex.isInitiallyActive() )
        {
            addToActiveSet( 0L, vertex );
        }
    }
    
    void addToActiveSet( long superStep, VertexImpl vertex ) 
    { 
        superstepToActiveSetMap.get( superStep ).add( vertex ); 
    }
     
    void aggregateOutputProblemAggregator( Aggregator aggregator ) { outputProblemAggregator.aggregate(aggregator); }
    
    void aggregateOutputStepAggregator( Aggregator aggregator ) { outputStepAggregator.aggregate(aggregator); }
    
    ComputeOutput doSuperStep( ComputeThread computeThread, long superStep, ComputeInput computeInput )
    {
        this.computeThread = computeThread;
        this.superStep = superStep;
        this.computeInput = computeInput;
        numMessagesSent = 0;
        outputStepAggregator    = job.makeStepAggregator();
        outputProblemAggregator = job.makeProblemAggregator();
        Set<VertexImpl> activeSet = superstepToActiveSetMap.get( superStep );
        for ( VertexImpl vertex : activeSet )
        {
            vertex.compute();
            vertex.removeMessageQ( superStep );      // MessageQ now is garbage
        }
        superstepToActiveSetMap.remove( superStep ); // active vertex set now is garbage
        boolean thereIsNextStep = numMessagesSent > 0;
        return new ComputeOutput( thereIsNextStep, outputStepAggregator, outputProblemAggregator );
    }
        
    ComputeInput getComputeInput() { return computeInput; }
        
    int getPartId() { return partId; }
    
    long getSuperStep() { return superStep; }
    
    Vertex getVertex( int vertexId ) { return vertexIdToVertexMap.get( vertexId ); }
    
    Map<Object, VertexImpl> getVertexIdToVertexMap() { return vertexIdToVertexMap; }
    
    public Collection<VertexImpl> getVertices() { return vertexIdToVertexMap.values(); }
    
    void incrementNumMessagesSent() { numMessagesSent++; }
    
    void receiveMessage( Object vertexId, Message message, long superStep )
    {
        VertexImpl vertex = vertexIdToVertexMap.get( vertexId );
        assert vertex != null : vertexId;
        vertex.receiveMessage( message, superStep );
        addToActiveSet( superStep, vertex );
    }
    
    void receiveMessageQ( Object vertexId, MessageQ messageQ, long superStep )
    {
        VertexImpl vertex = vertexIdToVertexMap.get( vertexId );
        vertex.receiveMessageQ( messageQ, superStep );
        addToActiveSet( superStep, vertex );
    }
        
    void removeFromActiveSet( long superStep, VertexImpl vertex )
    {
        superstepToActiveSetMap.get( superStep ).remove( vertex );
    }
    
    void removeVertex( Object vertexId )
    {
        vertexIdToVertexMap.remove( vertexId );
        computeThread.removeVertex();
    }
    
    void sendMessage( Object vertexId, Message message, long superStep )
    {
        numMessagesSent++;
        int receivingPartId = job.getPartId( vertexId );
        if ( receivingPartId == this.partId )
        {
            receiveMessage( vertexId, message, superStep );
        }
        else
        {
            computeThread.sendMessage( receivingPartId, vertexId, message, superStep );
        }
    }
}
