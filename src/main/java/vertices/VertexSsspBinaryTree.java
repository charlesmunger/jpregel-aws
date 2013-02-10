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
public class VertexSsspBinaryTree extends VertexShortestPath
{
    private static final Integer MAX_VALUE = new Integer( Integer.MAX_VALUE );
    private static final Integer ZERO = new Integer( 0 );
    private static final Integer ONE = new Integer( 1 );
    
    public VertexSsspBinaryTree() {}
    
    public VertexSsspBinaryTree( Integer vertexId, Message<Integer, Integer> vertexValue, Map<Integer, Integer> edgeMap )
    { 
        super( vertexId, edgeMap , 2);
        setVertexValue( new Message( vertexId, MAX_VALUE ) );
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
        Integer initialKnownDistance = vertexId.equals(ONE) ? ZERO : MAX_VALUE;
        Message<Integer, Integer> vertexValue = new Message<Integer, Integer>( ONE, initialKnownDistance );
        Map<Integer, Integer> edgeMap = new HashMap<Integer, Integer>( numChildren );
        switch ( numChildren )
        {
            case 2: edgeMap.put( 2 * vertexId + 1, ONE );
            case 1: edgeMap.put( 2 * vertexId,     ONE );
            default: break; // no children
        }
        return new VertexSsspBinaryTree( vertexId, vertexValue, edgeMap );
    }
    
    /*
     * Assumption: numParts is a power of 2.
     */
    @Override
    public int getPartId( Integer vertexId, int numParts )
    {
        int shiftAmount = Integer.numberOfLeadingZeros( numParts ) - Integer.numberOfLeadingZeros( vertexId );
        int partId = ( shiftAmount >= 0 ) ? vertexId >> shiftAmount : vertexId << -shiftAmount;
//        System.out.println("VertexSsspBinaryTree.getPartId: vertexId: " + vertexId 
//                + " numParts: " + numParts 
//                + " Integer.numberOfLeadingZeros( numParts ): " + Integer.numberOfLeadingZeros( numParts )
//                + " Integer.numberOfLeadingZeros( vertexId ): " + Integer.numberOfLeadingZeros( vertexId )
//                + " shiftAmount: " + shiftAmount
//                + " partId: " + partId
//                + " returned partId: " + (partId - numParts));
        return partId - numParts;
    }
}
