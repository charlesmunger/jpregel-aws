package edu.ucsb.jpregel.system;

import api.Aggregator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Immutable
 *
 * @author Pete Cappello
 */
final public class ComputeOutput implements java.io.Serializable
{
    final private boolean thereIsANextStep;
    final private Map<Integer, Map<Object, MessageQ>> workerNumToVertexIdToMessageQMapMap;
    final private Aggregator    stepAggregator;
    final private Aggregator    problemAggregator;
    final private AtomicInteger deltaNumVertices;
    
    // Used by Vertex
    ComputeOutput( boolean thereIsANextStep,
            Map<Integer, Map<Object, MessageQ>> workerNumToVertexIdToMessageQMapMap, 
            Aggregator stepAggregator, Aggregator problemAggregator )
    {
        this.thereIsANextStep  = thereIsANextStep;
        this.workerNumToVertexIdToMessageQMapMap = workerNumToVertexIdToMessageQMapMap;
        this.stepAggregator    = stepAggregator;
        this.problemAggregator = problemAggregator;
        deltaNumVertices = new AtomicInteger();
    }
    
    ComputeOutput( boolean thereIsANextStep, Aggregator stepAggregator, Aggregator problemAggregator, AtomicInteger deltaNumVertices )
    {
        this.thereIsANextStep  = thereIsANextStep;
        this.workerNumToVertexIdToMessageQMapMap = null;
        this.stepAggregator    = stepAggregator;
        this.problemAggregator = problemAggregator;
        this.deltaNumVertices  = deltaNumVertices;
    }
    
    boolean getThereIsANextStep()  { return thereIsANextStep; }
    
    Map<Integer, Map<Object, MessageQ>> getWorkerNumToVertexIdToMessageQMapMap()
    {
        return workerNumToVertexIdToMessageQMapMap;
    }
    
    Aggregator getStepAggregator()    { return stepAggregator; }

    Aggregator getProblemAggregator() { return problemAggregator; }
    
    int deltaNumVertices()     { return deltaNumVertices.get(); }
}
