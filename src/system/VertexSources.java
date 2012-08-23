package system;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 *
 * @author Pete Cappello
 */
public class VertexSources extends VertexImpl<Integer, Boolean, Integer, Integer>
{
    public VertexSources() {}
    
    public VertexSources( Integer vertexId, Map<Integer, Integer> edgeMap )
    {
        super( vertexId, edgeMap );
    }
    
    @Override
    public void compute() 
    {
        if ( null == getVertexValue() ) {
            setVertexValue( true ); // initially mark all vertices as sources
            
            // for each out-edge, send a message to the endpoint (aka target vertex)
            for ( Integer targetVertexId : getEdgeTargets() ) {
                sendMessage( targetVertexId, new Message( null, null ) );
            }
        }
        else {
            if ( getMessageQ().size() > 0 ) {
                setVertexValue( false ); // I have at least 1 in-edge!
            }
        }
    }

    @Override
    public String output() { return getVertexId() + " " + getVertexValue(); }
    
    @Override
    public boolean isSource() { return true; } // unused


    @Override
    public VertexImpl make(String line)
    {
        StringTokenizer stringTokenizer = new StringTokenizer( line );
        int vertexId = Integer.parseInt( stringTokenizer.nextToken() );
        Map<Integer, Integer> edgeMap = new HashMap<Integer, Integer>();
        while( stringTokenizer.hasMoreTokens() )
        { 
            int targetVertexId = Integer.parseInt( stringTokenizer.nextToken() );
            edgeMap.put( targetVertexId, null );
        }
        return new VertexSources( vertexId, edgeMap );
    }
}
