package edu.ucsb.jpregel.system;

import api.Aggregator;

/**
 * Immutable
 *
 * @author Pete Cappello
 */
public class ComputeInput implements java.io.Serializable
{
    private Aggregator problemAggregator;
    private int        numVertices;
    
    ComputeInput( Aggregator problemAggregator, int numVertices )
    {
        this.problemAggregator = problemAggregator;
        this.numVertices = numVertices;
    }
    
    int        getNumVertices()       { return numVertices; }
    
    Aggregator getProblemAggregator() { return problemAggregator; }
}
