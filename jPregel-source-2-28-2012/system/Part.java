package system;

import static java.lang.System.out;
import java.util.Collection;
import java.util.Collections;

import java.util.HashMap;
import java.util.Map;

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
    private boolean isActive; // when at least 1 of its Vertex objects isActive
    private ComputeThread computeThread;
    
    
    
    Part( int partId, Worker worker )
    {
        this.partId = partId;
        this.worker = worker;
        workerJob = worker.getWorkerJob();
    }
    
    void add( Vertex vertex )
    {
        vertex.setPart( this );
        vertexIdToVertexMap.put( vertex.getVertexId(), vertex ); 
    }
    
    ComputeOutput doSuperStep( ComputeThread computeThread, ComputeInput computeInput )
    {
        this.computeThread = computeThread;
        int numActiveVertices = 0;
        int numMessagesSent   = 0;
        Aggregator outputStepAggregator    = workerJob.makeStepAggregator();
        Aggregator outputProblemAggregator = workerJob.makeProblemAggregator();
        for ( Vertex vertex : vertexIdToVertexMap.values() )
        {
            vertex.advanceStep();
            if ( vertex.isActive() )
            {
                vertex.setInput( computeInput );
                vertex.compute();
//                vertex.removeMessageQ( vertex.getSuperStep() ); // MessageQ is garbage
                outputStepAggregator.aggregate(    vertex.getOutputStepAggregator()    );
                outputProblemAggregator.aggregate( vertex.getOutputProblemAggregator() );
                if ( vertex.isNextStepActive() )
                {
                    numActiveVertices++;
                }
                numMessagesSent += vertex.getNumMessagesSent();
            }
        }
        boolean thereIsNextStep = numMessagesSent > 0 || numActiveVertices > 0;
        ComputeOutput computeOutput = new ComputeOutput( thereIsNextStep, outputStepAggregator, outputProblemAggregator );
        return computeOutput;
    }
        
    int getPartId() { return partId; }
    
    Vertex getVertex( int vertexId ) { return vertexIdToVertexMap.get( vertexId ); }
    
    Map<Object, Vertex> getVertexIdToVertexMap() { return vertexIdToVertexMap; }
    
    public Collection<Vertex> getVertices() { return vertexIdToVertexMap.values(); }
    
    synchronized void receiveMessage( Object vertexId, Message message, long superStep )
    {
        Vertex vertex = vertexIdToVertexMap.get( vertexId );
        // DEBUG
        if ( vertex == null )
        {
            out.println("Part.receiveMessage: vertexId: " + vertexId );
        }
        vertex.receiveMessage( message, superStep );
    }
    
    synchronized void receiveMessageQ( Object vertexId, MessageQ messageQ, long superStep )
    {
        Vertex vertex = vertexIdToVertexMap.get( vertexId );
        vertex.receiveMessageQ( messageQ, superStep );
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
