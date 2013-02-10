package edu.ucsb.jpregel.system;

import static java.lang.System.err;
import static java.lang.System.exit;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 *
 * The value associated with each vertex, v, is: 
 * 1. the minimum distance to vertex v
 * 2. the vertexId of vertex u, where edge (u, v) is on a shortest path to v.
 * Since these 2 items constitute a Message, vertexValue's type is Message.
 * 
 * @author Pete Cappello
 */
public class VertexShortestPath extends VertexImpl<Integer, Message<Integer, Integer>, Integer, Integer>
{
    private static final Combiner sCombiner = new CombinerMinInteger();
    private static final ThreadLocal< StringBuilder> uniqueNum = new ThreadLocal<StringBuilder>() {

		@Override
		protected StringBuilder initialValue() {
			return new StringBuilder();
		}
	};
    
    public VertexShortestPath( Integer vertexId, Map<Integer, Integer> edgeMap, int numOutgoingEdges )
    {
        super( vertexId, edgeMap, numOutgoingEdges );
        initialValue( vertexId);
        combiner = sCombiner;
    }
    
    public VertexShortestPath( Integer vertexId, Map<Integer, Integer> edgeMap ) 
    {
        super( vertexId, edgeMap );
        initialValue( vertexId);
        combiner = sCombiner;
    }
    
    public VertexShortestPath() {}
    
    @Override
    public VertexImpl make( String line )
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
        return new VertexShortestPath( vertexId, edgeMap );
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
                Integer edgeValue = getEdgeMap().get( targetVertexId );
                Message message = new Message( getVertexId(), minDistanceMessage.getMessageValue() + edgeValue );
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
        StringBuilder stringBuilder = uniqueNum.get();
        stringBuilder.append( "" );
        if ( getNumVertices() - 10 <= getVertexId() )
        {
            System.out.println("   VertexShortestPath.output: VertexId: " + getVertexId());
            stringBuilder.append( getVertexId() );
            stringBuilder.append( " : ");
            stringBuilder.append( getVertexValue().getVertexId() );
            stringBuilder.append( " - ");
            stringBuilder.append( getVertexValue().getMessageValue() );
        }
        String toString = stringBuilder.toString();
        stringBuilder.setLength(0);
        return toString;
    }
        
    @Override
    public boolean isInitiallyActive() { return isSource(); }
    
    public boolean isSource() { return getVertexId() == 1; }

    public void initialValue(Integer vertexId) {
        setVertexValue( new Message<Integer, Integer>( vertexId, Integer.MAX_VALUE ) );
    }
}
