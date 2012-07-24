package system;
 
import static java.lang.System.out;
import java.util.*;

/**
 *
 * @author Peter Cappello
 */
public final class Part
{
    private final int partId;
    private final Worker worker; // to which this part is assigned
    private final Job job;
    
    private Map<Object, Vertex> vertexIdToVertexMap = Collections.synchronizedMap( new HashMap<Object, Vertex>() );   
    private SuperStepToActiveSetMap superstepToActiveSetMap = new SuperStepToActiveSetMap();
    
    // superStep parameters
    private ComputeThread computeThread;
    private long superStep;
    private ComputeInput computeInput;
    private Aggregator outputProblemAggregator;
    private Aggregator outputStepAggregator;
    /*
     * numMessagesSent is used by the Vertex.compute method: 
     * The set of vertices must have their compute methods invoked sequentially.
     */
    private int numMessagesSent; 
    
    Part( int partId, Worker worker )
    {
        this.partId = partId;
        this.worker = worker;
        job = worker.getJob();
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
            if ( superstepToActiveSetMap == null )
            {
                System.out.println("Part.add: superstepToActiveSetMap: " + superstepToActiveSetMap);
            }
            superstepToActiveSetMap.get( 0 ).add( vertex );
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
            out.println("Part.receiveMessage: vertexId: " + vertexId );
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
