package edu.ucsb.jpregel.system;

import static java.lang.System.err;
import static java.lang.System.exit;
import static java.lang.System.out;


import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 *
 * @author Pete Cappello
 */
class VertexTestMutability extends VertexImpl<Integer, Message<Integer, Integer>, Integer, Integer>
{
    public VertexTestMutability( Integer vertexId, Map<Integer, Integer> edgeMap )
    {
        super( vertexId, edgeMap );
        setVertexValue( new Message<Integer, Integer>( vertexId, Integer.MAX_VALUE ) );
    }
    
    public VertexTestMutability() {}
    
    @Override
    public VertexImpl make( String line )
    {
        StringTokenizer stringTokenizer = new StringTokenizer( line );
        if ( ! stringTokenizer.hasMoreTokens() )
        {
            err.println( "TestMutabilityVertex.make: Empty lines are not allowed." );
            exit( 1 );
        }
        int vertexId = Integer.parseInt( stringTokenizer.nextToken() );
        Map<Integer, Integer> edgeMap = new HashMap<Integer, Integer>();
        while( stringTokenizer.hasMoreTokens() )
        { 
            int target  = Integer.parseInt( stringTokenizer.nextToken() );
            int weight  = Integer.parseInt( stringTokenizer.nextToken() ); 
            edgeMap.put( target, weight );
        }
//        return new VertexTestMutability( vertexId, outEdgeMap, combiner );
        return new VertexTestMutability( vertexId, edgeMap );
    }
    
    @Override
    public void compute() 
    {
        if ( getSuperStep() == 0 && getVertexId() == 0 )
        {
            addEdge( new Integer( 2 ), -1 );
        }
        if ( getSuperStep() == 1 && getVertexId() == 0 )
        {
            removeEdge( new Integer( 1 ) );
        }
        if ( getSuperStep() == 2 && getVertexId() == 1 )
        {
            out.println("TestMutabilityVertex.compute: step: " + getSuperStep() + " numVertices: " + getNumVertices() );
            removeVertex();
        }
        if ( getSuperStep() == 3 && getVertexId() == 0 )
        {
            out.println("TestMutabilityVertex.compute: step: numVertices: " + getNumVertices() );
        }
        
        /* This vote will be overturned, if during this step, a vertex for whom 
         * I am a target vertex discovered a shorter path to itself, 
         * in which case, it will send me a message.
         */   
//        if ( getSuperStep() == 3 )
//        {
//            voteToHalt(); 
//        }
    }

    @Override
    public String output() 
    {
        StringBuilder string = new StringBuilder();
        string.append( getVertexId() );
        string.append( " : ");
//        string.append( ((Message)  getVertexValue() ).getMessageValue() );
        for ( Integer targetId : getEdgeMap().keySet() )
            {
                string.append( targetId );
                string.append( " ");
                string.append( getEdgeMap().get( targetId ) );
                string.append( " ");
            }
        return new String( string );
    }
    
    @Override
    public boolean isInitiallyActive() { return isSource(); }
    
    public boolean isSource() { return getVertexId() == 0; }
}