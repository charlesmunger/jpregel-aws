 package vertex;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import system.Combiner;
import system.Job;
import system.Message;
import system.Vertex;

/**
 *
 * @author Pete Cappello
 */
public final class BinaryTreeShortestPathVertex extends Vertex<Integer, Message<Integer, Integer>, Integer, Integer>
{
    private static int      numVertices; //TODO these fields hide Vertex's fields.
    public  static Combiner combiner;
    
//    private Pattern regex = Pattern.compile(" ");
        
    public BinaryTreeShortestPathVertex() {}
    
    public BinaryTreeShortestPathVertex( Integer vertexId, Message<Integer, Integer> vertexValue, Map<Integer, Integer> edgeMap )
    { 
        super( vertexId, edgeMap );
        setVertexValue( new Message( vertexId, Integer.MAX_VALUE ) );
    }
    
    @Override
    synchronized public Vertex make( String line )
    {
//        String[] tokenArray = regex.split(line, 2);
//        Integer vertexId = Integer.parseInt( tokenArray[0] );
//        numVertices      = Integer.parseInt( tokenArray[1] );
        
        // Here, tokenizer is faster than split
        StringTokenizer stringTokenizer = new StringTokenizer( line );
        Integer vertexId = Integer.parseInt( stringTokenizer.nextToken() );
        numVertices      = Integer.parseInt( stringTokenizer.nextToken() );

        Integer initialKnownDistance = ( vertexId == 1 ) ? 0 : Integer.MAX_VALUE;
        Message<Integer, Integer> vertexValue = new Message<Integer, Integer>( 1, initialKnownDistance );
        
        Map<Integer, Integer> edgeMap = new HashMap<Integer, Integer>( 2 );
        Integer targetVertexId = 2 * vertexId; // << 1;
        if ( targetVertexId <= numVertices )
        {   // make OutEdge for left child
            edgeMap.put( targetVertexId, 1 );

            if ( targetVertexId < numVertices )
            {   // make OutEdge for right child
                edgeMap.put( ++targetVertexId, 1 );
            }
        }
        
        return new BinaryTreeShortestPathVertex( vertexId, vertexValue, edgeMap );
    }
    
    @Override     
    public void compute() 
    {
        // compute currently known minimum distance from source to me via messaging vertices
        int minDistance = isSource() ? 0 : Integer.MAX_VALUE;
        Message<Integer, Integer> minDistanceMessage = new Message<Integer, Integer>( getVertexId(), minDistance );
        for ( Message<Integer, Integer> message : getMessageQ() )
        {
            if ( message.getMessageValue() < minDistanceMessage.getMessageValue() )
            {
                minDistanceMessage = message;
            }
        }
        
        if ( minDistanceMessage.getMessageValue() < getVertexValue().getMessageValue() )
        {   // there is a new shorter path from the source to me
            setVertexValue( minDistanceMessage ); // update my value: the shortest path to me

            // To each target vertex: The shortest known path to you through me just got shorter
            for ( Integer targetId : edgeMap.keySet() )            
            {
                Message<Integer, Integer> message = new Message<Integer, Integer>( getVertexId(), minDistanceMessage.getMessageValue() + 1 );   
                sendMessage( targetId, message );
            }
        }
    }
    
    synchronized public int getPartId( Integer vertexId, Job job ) { return vertexId / 1000; }
    
    @Override
    public String output() 
    {
        StringBuilder string = new StringBuilder();
        string.append( getVertexId() );
        string.append( " : ").append( getVertexValue().getVertexId() );
        string.append( " - ").append( getVertexValue().getMessageValue() );
        return new String( string );
    }
    
    @Override
    synchronized protected boolean isSource() { return getVertexId() == 1; }
}
