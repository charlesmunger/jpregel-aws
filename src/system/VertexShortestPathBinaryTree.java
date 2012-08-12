 package system;

import system.VertexShortestPath;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import system.Job;
import system.Message;
import system.VertexImpl;

/**
 *
 * @author Pete Cappello
 */
public final class VertexShortestPathBinaryTree extends VertexShortestPath
{
    public VertexShortestPathBinaryTree() {}
    
    public VertexShortestPathBinaryTree( Integer vertexId, Message<Integer, Integer> vertexValue, Map<Integer, Integer> edgeMap )
    { 
        super( vertexId, edgeMap );
        setVertexValue( new Message( vertexId, Integer.MAX_VALUE ) );
    }
    
    @Override
    synchronized public VertexImpl make( String line )
    {
        StringTokenizer stringTokenizer = new StringTokenizer( line );
        Integer vertexId = Integer.parseInt( stringTokenizer.nextToken() );
        int numChildren  = Integer.parseInt( stringTokenizer.nextToken() );
        return make( vertexId, numChildren);
    }
    
    synchronized public VertexImpl make( Integer vertexId, int numChildren )
    {
        Integer initialKnownDistance = ( vertexId == 1 ) ? 0 : Integer.MAX_VALUE;
        Message<Integer, Integer> vertexValue = new Message<Integer, Integer>( 1, initialKnownDistance );
        Map<Integer, Integer> edgeMap = new HashMap<Integer, Integer>( numChildren );
        switch ( numChildren )
        {
            case 2: edgeMap.put( 2 * vertexId + 1, 1 );
            case 1: edgeMap.put( 2 * vertexId,     1 );
            default: break; // no children
        }
        return new VertexShortestPathBinaryTree( vertexId, vertexValue, edgeMap );
    }
    
    public int getPartId( Integer vertexId, Job job ) { return vertexId / 1000; }
}
