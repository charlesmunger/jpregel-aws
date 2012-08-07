package vertex;

import static java.lang.System.err;
import static java.lang.System.exit;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import system.Combiner;
import system.Message;
import system.OutEdge;
import system.Vertex;
import system.aggregators.IntegerSumAggregator;
import system.combiners.IntegerMinCombiner;

/**
 *
 * The value associated with each vertex, v, is: 
 * 1. the minimum distance to vertex v
 * 2. the vertexId of vertex u, where edge (u, v) is on a shortest path to v.
 * Since these 2 items constitute a Message, vertexValue is a Message.
 * 
 * @author Pete Cappello
 */
public final class ShortestPathVertex extends Vertex<Integer, Message<Integer, Integer>, Integer, Integer>
{
    public ShortestPathVertex( Integer vertexId, Map<Integer, Integer> edgeMap )
    {
        super( vertexId, edgeMap );
        setVertexValue( new Message<Integer, Integer>( vertexId, Integer.MAX_VALUE ) );
        combiner = new IntegerMinCombiner();
    }
    
    public ShortestPathVertex() {}
    
    @Override
    public Vertex make( String line )
    {
        StringTokenizer stringTokenizer = new StringTokenizer( line );
        if ( ! stringTokenizer.hasMoreTokens() )
        {
            err.println( "ShortestPathVertex.make: Empty lines are not allowed." );
            exit( 1 );
        }
        int vertexId = Integer.parseInt( stringTokenizer.nextToken() );
        Map<Integer, Integer> edgeMap = new HashMap<Integer, Integer>();
        while( stringTokenizer.hasMoreTokens() )
        { 
            int target = Integer.parseInt( stringTokenizer.nextToken() );
            int weight = Integer.parseInt( stringTokenizer.nextToken() ); 
            edgeMap.put( target, weight );
        }
        return new ShortestPathVertex( vertexId, edgeMap );
    }
    
    @Override
    public void compute() 
    {
        // compute currently known minimum distance from source to me
        int minDistance = isSource() ? 0 : Integer.MAX_VALUE;
        Message<Integer, Integer> minDistanceMessage = new Message<Integer, Integer>( getVertexId(), minDistance );
        for ( Message<Integer, Integer> message : getMessageQ() )
        {
            if ( message.getMessageValue() < minDistanceMessage.getMessageValue() )
            {
                minDistanceMessage = message;
            }
        }
        if ( minDistanceMessage.getMessageValue() < ((Message<Integer, Integer>) getVertexValue() ).getMessageValue() )
        {   // There is a new shorter path from the source to me
            setVertexValue( minDistanceMessage ); // update my value: the shortest path to me
            
            // To each of my target vertices: The shortest known path to you through me just got shorter 
            for ( Integer targetVertexId : getEdgeTargets() )
            {
                Integer edgeValue = edgeMap.get( targetVertexId );
                Message message = new Message( getVertexId(), minDistanceMessage.getMessageValue() + (Integer) edgeValue );
                sendMessage( targetVertexId, message );
            }
            
            // aggregate number of messages sent in this step & this problem
//            aggregateOutputProblemAggregator( new IntegerSumAggregator( getEdgeMapSize() ));
//            aggregateOutputStepAggregator(    new IntegerSumAggregator( getEdgeMapSize() ));
        }
        
        /* This vote will be overturned, if during this step, a vertex for whom 
         * I am a target vertex discovers a shorter path to itself, 
         * in which case, it will send me a message.
         */   
//        voteToHalt(); 
    }

    @Override
    public String output() 
    {
        StringBuilder string = new StringBuilder();
        string.append( getVertexId() );
        string.append( " : ");
        string.append( getVertexValue().getVertexId() );
        string.append( " ");
        string.append( getVertexValue().getMessageValue() );
        return new String( string );
    }
    
    @Override
    protected boolean isSource() { return getVertexId() == 0; }
}
