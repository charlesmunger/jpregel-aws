package system;

import static java.lang.System.err;
import static java.lang.System.exit;

import static java.lang.Math.sqrt;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 *
 * The value associated with each vertex, v, is: 
 * 1. the minimum distance to vertex v
 * 2. the vertexId of vertex u, where edge (u, v) is on a shortest path to v.
 * Since these 2 items constitute a Message, vertexValue is a Message.
 * 
 * @author Pete Cappello
 */
public final class VertexShortestPathEuclidean extends VertexImpl<Point2D.Float, Message<Point2D.Float, Float>, Point2D.Float, Float>
{
    public static Combiner combiner = new CombinerMinFloat(); //TODO this field hides another
    
    public VertexShortestPathEuclidean( Point2D.Float vertexId, Map<Point2D.Float, Point2D.Float> edgeMap )
    {
        super( vertexId, edgeMap );
        setVertexValue( new Message( vertexId, Float.MAX_VALUE ) );
    }
    
    public VertexShortestPathEuclidean() {} // needed to make vertexFactory
    
    @Override
    public VertexImpl make( String line )
    {
        StringTokenizer stringTokenizer = new StringTokenizer( line );
        if ( ! stringTokenizer.hasMoreTokens() )
        {
            err.println( "EuclideanShortestPathVertex.make: Empty lines are not allowed: line: '" + line + "'");
            exit( 1 );
        }
        float vx  = Float.parseFloat( stringTokenizer.nextToken() );
        float vy  = Float.parseFloat( stringTokenizer.nextToken() );
        Point2D.Float vertexId = new Point2D.Float( vx, vy);
        Map<Point2D.Float, Point2D.Float> edgeMap = new HashMap<Point2D.Float, Point2D.Float>();
        while( stringTokenizer.hasMoreTokens() )
        { 
            float x = Float.parseFloat( stringTokenizer.nextToken() );
            float y = Float.parseFloat( stringTokenizer.nextToken() );
            Point2D.Float target = new Point2D.Float( x, y );
            // TODO VertexShortestPathEuclidean extends UnweightedEdgeVertex which extends VertexImpl
            edgeMap.put( target, target );
        }
        
        // initialize vertexValue
        Float minDistance = ( vx == 0.0 && vy == 0.0 ) ? (float) 0.0 : Float.MAX_VALUE;
        Message<Point2D.Float, Float> minDistanceMessage = new Message<Point2D.Float, Float>( new Point2D.Float(), minDistance );
        setVertexValue( minDistanceMessage );
        
        return new VertexShortestPathEuclidean( vertexId, edgeMap );
    }  

    @Override     
    public void compute() 
    {
        // compute currently known minimum distance from source to me
        Float minDistance = isSource() ? (float) 0.0 : Float.MAX_VALUE;
        Message<Point2D.Float, Float> minDistanceMessage = new Message<Point2D.Float, Float>( getVertexId(), minDistance );
        for ( Message<Point2D.Float, Float> message : getMessageQ() )
        {
            if ( message.getMessageValue() < minDistanceMessage.getMessageValue() )
            {
                minDistanceMessage = message;
            }
        }
        
        if ( minDistanceMessage.getMessageValue() < getVertexValue().getMessageValue() )
        {
            // found a new shorter path from the source to me
            setVertexValue( minDistanceMessage ); // update my value: the shortest path to me
            
            // To each target vertex: The shortest known path to you through me just got shorter 
            for ( Point2D.Float targetVertexId : getEdgeMap().keySet() )            
            {
                float edgeValue = distance( targetVertexId );
                Message<Point2D.Float, Float> message = new Message<Point2D.Float, Float>( getVertexId(), minDistanceMessage.getMessageValue() + edgeValue );   
                sendMessage( targetVertexId, message );
            }
            
            // aggregate number of messages sent in this step & in this problem
//            aggregateOutputProblemAggregator( new IntegerSumAggregator( getOutEdgeMapSize() ));
//            aggregateOutputStepAggregator(    new IntegerSumAggregator( getOutEdgeMapSize() ));
        }
   
        /* This vote will be overturned, if during this step, a vertex for whom 
         * I am a target vertex discovered a shorter path to itself, 
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
        string.append(  getVertexValue().getVertexId() );
        string.append( " - ");
        string.append( getVertexValue().getMessageValue() );
        return new String( string );
    }
    
    /*
     * Would prefer to map square subgrids to a part.
     */
    @Override
    public int getPartId( Point2D.Float vertexId, int numParts )
    {
        int row = (int) vertexId.getX();
        return row % numParts;
    }
    
    @Override
    public boolean isSource()
    {
        Point2D.Float vertex = (Point2D.Float) getVertexId();
        return vertex.getX() == 0.0 && vertex.getY() == 0.0; 
    }
    
    private float distance( Point2D.Float targetVertexId )
    {
        double x1 = getVertexId().getX();
        double y1 = getVertexId().getY();
        double x2 = targetVertexId.getX();
        double y2 = targetVertexId.getY();
        double deltaX = x1 - x2;
        double deltaY = y1 - y2;
        return (float) sqrt( deltaX * deltaX + deltaY * deltaY );
    }
}
