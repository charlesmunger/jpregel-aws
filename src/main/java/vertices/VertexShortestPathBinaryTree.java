 package vertices;

import edu.ucsb.jpregel.system.Message;
import edu.ucsb.jpregel.system.VertexImpl;
import edu.ucsb.jpregel.system.VertexShortestPath;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 *
 * @author Pete Cappello
 */
public final class VertexShortestPathBinaryTree extends VertexShortestPath
{
    private static final Integer MAX = new Integer(Integer.MAX_VALUE);
    
    public VertexShortestPathBinaryTree() {}
    
    public VertexShortestPathBinaryTree( Integer vertexId, Map<Integer, Integer> edgeMap )
    { 
        super( vertexId, edgeMap ,edgeMap.size());
        setVertexValue( new Message( vertexId, Integer.MAX_VALUE ) );
    }
    
    @Override
    public VertexImpl make( String line )
    {
        StringTokenizer stringTokenizer = new StringTokenizer( line );
        Integer vertexId = Integer.parseInt( stringTokenizer.nextToken() );
        int numChildren  = Integer.parseInt( stringTokenizer.nextToken() );
        return make( vertexId, numChildren);
    }
    
    public VertexImpl make( Integer vertexId, int numChildren )
    {
        Map<Integer, Integer> edgeMap = new HashMap<Integer, Integer>( numChildren );
        if(vertexId.intValue() == 5000000) {
            System.out.println("debug");
        }
        switch ( numChildren )
        {
            case 2: edgeMap.put( 2 * vertexId + 1, 1 );
            case 1: edgeMap.put( 2 * vertexId,     1 ); 
            case 0: break;
            default: System.err.println("More than two children?" + vertexId + " num: "+ numChildren);
                break; // no children
        }
        return new VertexShortestPathBinaryTree( vertexId, edgeMap );
    }
    
    @Override
    public int getPartId(Integer idi, int numparts) {
        int id = idi.intValue();
        numparts--;
        if(id < (numparts)) {
            return 0;
        }
        while(((id >> 1) >= (numparts))) {
            id >>=1;
        }
        return id-numparts+1;
    }
    
    @Override
    public void initialValue(Integer vertexId) {
          setVertexValue( new Message( vertexId, MAX) );
    }
}
