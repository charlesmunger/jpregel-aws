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
    private final WorkerJob workerJob;
    
    private Map<Object, Vertex> vertexIdToVertexMap = Collections.synchronizedMap( new HashMap<Object, Vertex>() );   
    private SuperStepToActiveSetMap superstepToActiveSetMap = new SuperStepToActiveSetMap();
    private ComputeThread computeThread;
    private long superStep;
    
    Part( int partId, Worker worker )
    {
        this.partId = partId;
        this.worker = worker;
        workerJob = worker.getWorkerJob();
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
    
    void addToActiveSet( long superStep, Vertex vertex )
    {
        superstepToActiveSetMap.get( superStep ).add( vertex );
    }
    
    ComputeOutput doSuperStep( ComputeThread computeThread, long superStep, ComputeInput computeInput )
    {
        this.computeThread = computeThread;
        this.superStep = superStep;
        int numActiveVertices = 0;
        int numMessagesSent   = 0;
        Aggregator outputStepAggregator    = workerJob.makeStepAggregator();
        Aggregator outputProblemAggregator = workerJob.makeProblemAggregator();
//        for ( Vertex vertex : vertexIdToVertexMap.values() )
        Set<Vertex> currentActiveSet = superstepToActiveSetMap.get( superStep     );
        Set<Vertex>    nextActiveSet = superstepToActiveSetMap.get( superStep + 1 );
        for ( Vertex vertex : currentActiveSet )
        {
//            vertex.advanceStep();
//            if ( vertex.isActive() )
//            {
//                vertex.setInput( computeInput );
//                vertex.compute();
//                vertex.removeMessageQ( vertex.getSuperStep() ); // MessageQ is garbage
//                outputStepAggregator.aggregate(    vertex.getOutputStepAggregator()    );
//                outputProblemAggregator.aggregate( vertex.getOutputProblemAggregator() );
//                if ( vertex.isNextStepActive() )
//                {
//                    numActiveVertices++;
//                }
//                numMessagesSent += vertex.getNumMessagesSent();
//            }
//            System.out.println("Part.doSuperStep: step: " + superStep + " active vertex: " + vertex.getVertexId() );
            vertex.advanceStep(); // TODO Eliminate this method call
            vertex.setInput( computeInput );
            vertex.compute();
            vertex.removeMessageQ( superStep ); // MessageQ is garbage
            outputStepAggregator.aggregate(    vertex.getOutputStepAggregator()    );
            outputProblemAggregator.aggregate( vertex.getOutputProblemAggregator() );
            if ( vertex.isNextStepActive() )
            {
                numActiveVertices++;
                nextActiveSet.add( vertex );
                
            }
            numMessagesSent += vertex.getNumMessagesSent();
        }
        superstepToActiveSetMap.remove( superStep ); // current activeSet now is garbage
        boolean thereIsNextStep = numMessagesSent > 0 || numActiveVertices > 0;
        ComputeOutput computeOutput = new ComputeOutput( thereIsNextStep, outputStepAggregator, outputProblemAggregator );
        return computeOutput;
    }
        
    int getPartId() { return partId; }
    
    long getSuperStep() { return superStep; }
    
    Vertex getVertex( int vertexId ) { return vertexIdToVertexMap.get( vertexId ); }
    
    Map<Object, Vertex> getVertexIdToVertexMap() { return vertexIdToVertexMap; }
    
    public Collection<Vertex> getVertices() { return vertexIdToVertexMap.values(); }
    
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
        int receivingPartId = workerJob.getPartId( vertexId );
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
