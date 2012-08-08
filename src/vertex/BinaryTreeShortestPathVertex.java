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
public final class BinaryTreeShortestPathVertex extends ShortestPathVertex
{
    private static int      numVertices; //TODO these fields hide Vertex's fields.
    public  static Combiner combiner;
            
    public BinaryTreeShortestPathVertex() {}
    
    public BinaryTreeShortestPathVertex( Integer vertexId, Message<Integer, Integer> vertexValue, Map<Integer, Integer> edgeMap )
    { 
        super( vertexId, edgeMap );
        setVertexValue( new Message( vertexId, Integer.MAX_VALUE ) );
    }
    
    @Override
    synchronized public Vertex make( String line )
    {
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
    
    public int getPartId( Integer vertexId, Job job ) { return vertexId / 1000; }
}
