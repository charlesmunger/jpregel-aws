package system;
 
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Peter Cappello
 */
public final class Part
{
    private final int partId;
    private final Job job;
    
    // TODO Part: vertexIdToVertexMap: Consider constructing w/ a capacity of the Part's size.
    private Map<Object, Vertex> vertexIdToVertexMap = new ConcurrentHashMap<Object, Vertex>();
    private OntoMap<Set<Vertex>> superstepToActiveSetMap = new OntoMap<Set<Vertex>>( new ActiveSet() );
    // TODO remove SuperStepToActiveSetMap class
    
    // superStep parameters
    private ComputeThread computeThread;
    private long superStep;
    private ComputeInput computeInput;
    
    // The following 3 parameters are modified by each vertex during its compute method
    private Aggregator outputProblemAggregator;
    private Aggregator outputStepAggregator;
    private int numMessagesSent; 
    
    Part( int partId, Job job )
    {
        this.partId = partId;
        this.job    = job;
    }
    
    /*
     * FIX: For graph mutation (add vertex), also need void addVertexToActiveSet( Long superStep, Vertex vertex )
     */
    void add( Vertex vertex )
    {
        vertex.setPart( this );
        vertexIdToVertexMap.put( vertex.getVertexId(), vertex );
        if ( vertex.isSource() )
        {
            Set<Vertex> activeSet = superstepToActiveSetMap.get( new Long(0) );
            activeSet.add( vertex );
        }
    }
    
    void addToActiveSet( long superStep, Vertex vertex ) { superstepToActiveSetMap.get( superStep ).add( vertex ); }
    
    void aggregateOutputProblemAggregator( Aggregator aggregator ) { outputProblemAggregator.aggregate(aggregator); }
    
    void aggregateOutputStepAggregator( Aggregator aggregator ) { outputStepAggregator.aggregate(aggregator); }
    
    ComputeOutput doSuperStep( ComputeThread computeThread, long superStep, ComputeInput computeInput )
    {
        this.computeThread = computeThread;
        this.superStep = superStep;
        this.computeInput = computeInput;
        numMessagesSent   = 0;
        outputStepAggregator    = job.makeStepAggregator();
        outputProblemAggregator = job.makeProblemAggregator();
        Set<Vertex> currentActiveSet = superstepToActiveSetMap.get( superStep     );
        Set<Vertex>    nextActiveSet = superstepToActiveSetMap.get( superStep + 1 );
        for ( Vertex vertex : currentActiveSet )
        {
            vertex.compute();
            vertex.removeMessageQ( superStep ); // MessageQ is garbage
        }
        superstepToActiveSetMap.remove( superStep ); // current activeSet now is garbage
        boolean thereIsNextStep = numMessagesSent > 0;
        return new ComputeOutput( thereIsNextStep, outputStepAggregator, outputProblemAggregator );
    }
        
    ComputeInput getComputeInput() { return computeInput; }
        
    int getPartId() { return partId; }
    
    long getSuperStep() { return superStep; }
    
    Vertex getVertex( int vertexId ) { return vertexIdToVertexMap.get( vertexId ); }
    
    Map<Object, Vertex> getVertexIdToVertexMap() { return vertexIdToVertexMap; }
    
    public Collection<Vertex> getVertices() { return vertexIdToVertexMap.values(); }
    
    void incrementNumMessagesSent() { numMessagesSent++; }
    
    synchronized void receiveMessage( Object vertexId, Message message, long superStep )
    {
        Vertex vertex = vertexIdToVertexMap.get( vertexId );
        // BEGIN DEBUG
        if ( vertex == null )
        {
            System.out.println("Part.receiveMessage: vertexId: " + vertexId );
        }
        // END DEBUG
        vertex.receiveMessage( message, superStep );
        addToActiveSet( superStep, vertex );
    }
    
    synchronized void receiveMessageQ( Object vertexId, MessageQ messageQ, long superStep )
    {
        Vertex vertex = vertexIdToVertexMap.get( vertexId );
        vertex.receiveMessageQ( messageQ, superStep );
        addToActiveSet( superStep, vertex );
    }
        
    void removeFromActiveSet( long superStep, Vertex vertex )
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
