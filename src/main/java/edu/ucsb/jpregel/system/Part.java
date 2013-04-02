package edu.ucsb.jpregel.system;
 
import api.Aggregator;
import api.Vertex;
import java.util.Collection;
import java.util.HashMap;
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
    private Worker worker;
    
    private Map<Object, VertexImpl> vertexIdToVertexMap = new ConcurrentHashMapV8<Object, VertexImpl>( 8000 , 0.9f, 2);
    private OntoMap<Set<VertexImpl>> superstepToActiveSetMap = new OntoMap<Set<VertexImpl>>( new ActiveSet() );
    
    // superStep parameters
    private long superStep;
    private Map<Integer, Map<Object, MessageQ>> workerNumToVertexIdToMessageQMapMap;
    private ComputeInput computeInput;
    
    // superStep parameters modified by each vertex.compute()
    private Aggregator problemAggregator;
    private Aggregator stepAggregator;
    private int numMessagesSent; 
    
    Part( int partId, Job job, Worker worker )
    {
        this.partId = partId;
        this.job    = job;
        this.worker = worker;
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
     
    void aggregateOutputProblemAggregator( Aggregator aggregator ) { problemAggregator.aggregate(aggregator); }
    
    void aggregateOutputStepAggregator( Aggregator aggregator ) { stepAggregator.aggregate(aggregator); }
    
    ComputeOutput doSuperStep( long superStep, ComputeInput computeInput )
    {
        this.superStep = superStep;
        this.computeInput = computeInput;
        workerNumToVertexIdToMessageQMapMap = new HashMap<Integer, Map<Object, MessageQ>>();
        numMessagesSent = 0;
        stepAggregator    = job.makeStepAggregator();
        problemAggregator = job.makeProblemAggregator();
        Set<VertexImpl> activeSet = superstepToActiveSetMap.get( superStep );
        for ( VertexImpl vertex : activeSet )
        {
            vertex.compute();
            vertex.removeMessageQ( superStep );      // MessageQ now is garbage
        }
        superstepToActiveSetMap.remove( superStep ); // active vertex set now is garbage
        boolean thereIsNextStep = numMessagesSent > 0;
        return new ComputeOutput( thereIsNextStep, workerNumToVertexIdToMessageQMapMap, stepAggregator, problemAggregator );
    }
        
    ComputeInput getComputeInput() { return computeInput; }
        
    int getPartId() { return partId; }
    
    Aggregator getStepAggregator() { return stepAggregator; }
    
    long getSuperStep() { return superStep; }
    
    Vertex getVertex( int vertexId ) { return vertexIdToVertexMap.get( vertexId ); }
    
    Map<Object, VertexImpl> getVertexIdToVertexMap() { return vertexIdToVertexMap; }
    
    public Collection<VertexImpl> getVertices() { return vertexIdToVertexMap.values(); }
    
    void incrementNumMessagesSent() { numMessagesSent++; }
    
    void receiveMessage( Object vertexId, Message message, long superStep )
    {
        VertexImpl vertex = vertexIdToVertexMap.get( vertexId );
        assert vertex != null : vertexId;
        if ( vertex == null ) 
        {
            System.out.println( "VertexID null: " + vertexId );
        }
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
        worker.removeVertex();
    }
    
    void sendMessage( Object receivingVertexId, Message message, long superStep )
    {
        numMessagesSent++;
        int receivingPartId = job.getPartId( receivingVertexId );
        if ( receivingPartId == partId )
        {
            // message is for vertex in this Part
            receiveMessage( receivingVertexId, message, superStep );
            return;
        }
        
        // message is for a vertex in another part
        Part receivingPart = worker.getPart( receivingPartId );
        if ( receivingPart != null )
        {
            // receivingPart is internal to this Worker
            receivingPart.receiveMessage( receivingVertexId, message, superStep );
            return;
        }
        
        // receivingPart is external to this Worker
        int workerNum = worker.getWorkerNum( receivingPartId );
        assert workerNum != worker.getWorkerNum();
        
        //     get vertexIdToMessageQMap for destination Worker
        Map<Object, MessageQ> vertexIdToMessageQMap = workerNumToVertexIdToMessageQMapMap.get( workerNum );
        if ( vertexIdToMessageQMap == null )
        {
            vertexIdToMessageQMap = new HashMap<Object, MessageQ>();
            workerNumToVertexIdToMessageQMapMap.put( workerNum, vertexIdToMessageQMap );
        }

        //     get receivingVertex's MessageQ
        MessageQ receivingVertexMessageQ = vertexIdToMessageQMap.get( receivingVertexId );
        if ( receivingVertexMessageQ == null )
        {
            receivingVertexMessageQ = new MessageQ( job.getVertexFactory().getCombiner() );
            vertexIdToMessageQMap.put( receivingVertexId, receivingVertexMessageQ );
        }
        receivingVertexMessageQ.add( message );
    }
}
