package edu.ucsb.jpregel.system;

import api.Aggregator;

/**
 * Immutable
 *
 * @author Pete Cappello
 */
final public class ComputeOutput implements java.io.Serializable
{
    final private boolean    thereIsANextStep;
    final private Aggregator stepAggregator;
    final private Aggregator problemAggregator;
    final private int        deltaNumVertices;
    
    // Used by Vertex
    ComputeOutput( boolean thereIsANextStep, Aggregator stepAggregator, Aggregator problemAggregator )
    {
        this.thereIsANextStep  = thereIsANextStep;
        this.stepAggregator    = stepAggregator;
        this.problemAggregator = problemAggregator;
        deltaNumVertices = 0;
    }
    
    ComputeOutput( boolean thereIsANextStep, Aggregator stepAggregator, Aggregator problemAggregator, int deltaNumVertices )
    {
        this.thereIsANextStep  = thereIsANextStep;
        this.stepAggregator    = stepAggregator;
        this.problemAggregator = problemAggregator;
        this.deltaNumVertices  = deltaNumVertices;
    }
    
    boolean    getThereIsANextStep()  { return thereIsANextStep; }
    
    Aggregator getStepAggregator()    { return stepAggregator; }

    Aggregator getProblemAggregator() { return problemAggregator; }
    
    int        deltaNumVertices()     { return deltaNumVertices; }
}
