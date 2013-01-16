package edu.ucsb.jpregel.system;

import api.Aggregator;

/**
 * Immutable
 *
 * @author Pete Cappello
 */
public class ComputeInput implements java.io.Serializable
{
    private Aggregator stepAggregator;
    private Aggregator problemAggregator;
    private int        numVertices;
    
    ComputeInput( Aggregator stepAggregator, Aggregator problemAggregator, int numVertices )
    {
        this.stepAggregator    = stepAggregator;
        this.problemAggregator = problemAggregator;
        this.numVertices = numVertices;
    }
    
    int        getNumVertices()       { return numVertices; }
    
    Aggregator getStepAggregator()    { return stepAggregator;    }

    Aggregator getProblemAggregator() { return problemAggregator; }
}
