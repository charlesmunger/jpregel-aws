package system;

import api.Aggregator;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Pete Cappello
 */
public class ComputeThread extends Thread
{
    private final Worker worker;
    private boolean workIsAvailable;
    private Map<Integer, Part> partIdToPartMap;
    private Map<Integer, Map<Object, MessageQ>> workerNumToVertexIdToMessageQMapMap;
    private Combiner combiner;
    private ComputeInput computeInput;
    private int deltaNumVertices;
    private long superStep;
        
    ComputeThread( Worker worker ) { this.worker = worker; }
        
    void initJob()
    {
        workIsAvailable = false;
        partIdToPartMap = null;
        workerNumToVertexIdToMessageQMapMap = null;
        combiner = null;
    }
        
    @Override
    public void run()
    {
        while ( true )
        {
            // TODO Should this be refactored? Have ComputeThread get next available Part from Worker
            try
            {
                synchronized ( this )
                {
                    if ( ! workIsAvailable )
                    {
                        wait();
                    }
                    workIsAvailable = false;
                }
            }
            catch ( InterruptedException ignore ) {}
            
            combiner = worker.getJob().getVertexFactory().getCombiner();

            // compute parts of a superstep until there are no more parts
            boolean thereIsANextStep =  false;
            workerNumToVertexIdToMessageQMapMap = new HashMap<Integer, Map<Object, MessageQ>>();
            Aggregator outputStepAggregator     = worker.getJob().makeStepAggregator();
            Aggregator outputProblemAggregator  = worker.getJob().makeProblemAggregator(); //TODO: FIX? Make once/job not step!
            deltaNumVertices = 0;
            PartIterator partIterator = worker.getPartIterator();
            for ( Part part = partIterator.getPart(); part != null; part = partIterator.getPart() )
            {
                ComputeOutput computeOutput = part.doSuperStep( this, superStep, computeInput );
                thereIsANextStep |= computeOutput.getThereIsANextStep();
                outputStepAggregator.aggregate(    computeOutput.getStepAggregator() );
                outputProblemAggregator.aggregate( computeOutput.getProblemAggregator() );
            }
            ComputeOutput computeOutput = new ComputeOutput( thereIsANextStep, outputStepAggregator, outputProblemAggregator, deltaNumVertices );
            worker.computeThreadComplete( workerNumToVertexIdToMessageQMapMap, computeOutput ); // notify Worker of completion
        }  
    }
    
    void removeVertex() { deltaNumVertices--; }
    
    void sendMessage( int receivingPartId, Object vertexId, Message message, Long superStep )
    {
        Part receivingPart = partIdToPartMap.get( receivingPartId );
        if ( receivingPart != null )
        {
            // receivingPart is local to this Worker
            receivingPart.receiveMessage( vertexId, message, superStep );
        }
        else
        {
            // TODO ComputeThread: Later: monitor size of map: when too large, give to Worker to combine/send & reinitialize
            
            // get vertexIdToMessageQMap for destination Worker
            int workerNum = worker.getWorkerNum( receivingPartId );
            Map<Object, MessageQ> vertexIdToMessageQMap = workerNumToVertexIdToMessageQMapMap.get( workerNum );
            if ( vertexIdToMessageQMap == null )
            {
                vertexIdToMessageQMap = new HashMap<Object, MessageQ>();
                workerNumToVertexIdToMessageQMapMap.put( workerNum, vertexIdToMessageQMap );
            }
            
            // get MessageQ for vertex
            MessageQ messageQ = vertexIdToMessageQMap.get( vertexId );
            if ( messageQ == null )
            {
                messageQ = new MessageQ( combiner );
                vertexIdToMessageQMap.put( vertexId, messageQ );
            }
            messageQ.add( message );           
//            worker.sendMessage( receivingPartId, vertexId, message, superStep );
        }
    }
    
    void setPartIdToPartMap( Map<Integer, Part> partIdToPartMap ) { this.partIdToPartMap = partIdToPartMap; }
    
    synchronized void workIsAvailable( long superStep, ComputeInput computeInput )
    {
        this.computeInput = computeInput;
        this.superStep = superStep;
        workIsAvailable = true;
        notify();
    }
}
