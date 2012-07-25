package vertex;

import static java.lang.System.err;
import static java.lang.System.exit;

import static java.lang.Math.sqrt;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import system.*;
import system.aggregators.IntegerSumAggregator;
import system.combiners.FloatMinCombiner;

/**
 *
 * The value associated with each vertex, v, is: 
 * 1. the minimum distance to vertex v
 * 2. the vertexId of vertex u, where edge (u, v) is on a shortest path to v.
 * Since these 2 items constitute a Message, vertexValue is a Message.
 * 
 * @author Pete Cappello
 */
public final class EuclideanShortestPathVertex extends Vertex<Point2D.Float, Message, Point2D.Float, Float>
{
    public static Combiner combiner = new FloatMinCombiner();
    
    public EuclideanShortestPathVertex( Point2D.Float vertexId, Map<Object, Point2D.Float> outEdgeMap )
    {
        super( vertexId, outEdgeMap );
        setVertexValue( new Message( vertexId, Float.MAX_VALUE ) );
    }
    
    public EuclideanShortestPathVertex() {}
    
    public Vertex make( String line )
    {
        StringTokenizer stringTokenizer = new StringTokenizer( line );
        if ( ! stringTokenizer.hasMoreTokens() )
        {
            err.println( "EuclideanShortestPathVertex.make: Empty lines are not allowed: line: '" + line +"'");
            exit( 1 );
        }
        float vx  = Float.parseFloat( stringTokenizer.nextToken() );
        float vy  = Float.parseFloat( stringTokenizer.nextToken() );
        Point2D.Float vertexId = new Point2D.Float( vx, vy);
        Map<Object, Point2D.Float> outEdgeMap = new HashMap<Object, Point2D.Float>();
        while( stringTokenizer.hasMoreTokens() )
        { 
            float x  = Float.parseFloat( stringTokenizer.nextToken() );
            float y  = Float.parseFloat( stringTokenizer.nextToken() );
            Point2D.Float target = new Point2D.Float( x, y );
            outEdgeMap.put( target, target );
        }
        
        // initialize vertexValue
        // Cannot use isSource() to compute minDistance; vertex has not been created yet.
        Float minDistance = ( vx == 0.0 && vy == 0.0 ) ? (float) 0.0 : Float.MAX_VALUE;
        Message<Point2D.Float, Float> minDistanceMessage = new Message<Point2D.Float, Float>( new Point2D.Float(), minDistance );
        setVertexValue( minDistanceMessage );
        
        return new EuclideanShortestPathVertex( vertexId, outEdgeMap );
    }  

    @Override     
    public void compute() 
    {
        // compute currently known minimum distance from source to me
        Float minDistance = isSource() ? (float) 0.0 : Float.MAX_VALUE;
        Message<?, Float> minDistanceMessage = new Message<Point2D.Float, Float>( getVertexId(), minDistance );
        for ( Message<?, Float> message : getMessageQ() )
        {
            if ( message.getMessageValue() < minDistanceMessage.getMessageValue() )
            {
                minDistanceMessage = message;
            }
        }
        
        if ( minDistanceMessage.getMessageValue() < ((Message<Point2D.Float, Float>) getVertexValue() ).getMessageValue() )
        {
            // found a new shorter path from the source to me
            setVertexValue( minDistanceMessage ); // update my value: the shortest path to me
            
            // To each target vertex: The shortest known path to you through me just got shorter 
            for ( Point2D.Float targetVertexId : getOutEdgeValues() )            
            {
                float edgeValue = distance( targetVertexId );
                Message<Point2D.Float, Float> message = new Message<Point2D.Float, Float>( getVertexId(), minDistanceMessage.getMessageValue() + edgeValue );   
                sendMessage( targetVertexId, message );
            }
            
            // aggregate number of messages sent in this step & in this problem
            aggregateOutputProblemAggregator( new IntegerSumAggregator( getOutEdgeMapSize() ));
            aggregateOutputStepAggregator(    new IntegerSumAggregator( getOutEdgeMapSize() ));
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
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append( getVertexId() );
        stringBuffer.append( " : ");
        stringBuffer.append(  getVertexValue().getVertexId() );
        stringBuffer.append( " --- ");
        stringBuffer.append( getVertexValue().getMessageValue() );
        return new String( stringBuffer );
    }
    
    /*
     * Would prefer to map square subgrids to a part.
     */
    public int getPartId( Point2D.Float vertexId, int numParts )
    {
        int row = (int) vertexId.getX();
        return row % numParts;
    }
    
    protected boolean isSource()
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
