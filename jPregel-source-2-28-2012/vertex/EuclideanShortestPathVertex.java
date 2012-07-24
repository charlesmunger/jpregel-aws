package vertex;

import static java.lang.System.err;
import static java.lang.System.exit;
import static java.lang.System.out;

import static java.lang.Math.sqrt;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import system.*;
import system.aggregators.IntegerSumAggregator;

/**
 *
 * The value associated with each vertex, v, is: 
 * 1. the minimum distance to vertex v
 * 2. the vertexId of vertex u, where edge (u, v) is on a shortest path to v.
 * Since these 2 items constitute a Message, vertexValue is a Message.
 * 
 * @author Pete Cappello
 */
public final class EuclideanShortestPathVertex extends Vertex<Point2D.Float, Float>
{    
    public EuclideanShortestPathVertex( Point2D.Float vertexId, Map<Object, Point2D.Float> outEdgeMap, Combiner combiner )
    {
        super( vertexId, outEdgeMap, combiner );
        setVertexValue( new Message( vertexId, Float.MAX_VALUE ) );
    }
    
    public EuclideanShortestPathVertex() {}
    
    public Vertex make( String line, Combiner combiner )
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
        Message<Float> minDistanceMessage = new Message<Float>( new Point2D.Float(), minDistance );
        setVertexValue( minDistanceMessage );

        return new EuclideanShortestPathVertex( vertexId, outEdgeMap, combiner );
    }  

    @Override     
    public void compute() 
    {
        // compute currently known minimum distance from source to me
        Float minDistance = isSource() ? (float) 0.0 : Float.MAX_VALUE;
        Message<Float> minDistanceMessage = new Message<Float>( getVertexId(), minDistance );
        for ( Message<Float> message : getMessageQ() )
        {
            if ( message.getMessageValue() < minDistanceMessage.getMessageValue() )
            {
                minDistanceMessage = message;
            }
        }
        
        if ( minDistanceMessage.getMessageValue() < ((Message<Float>) getVertexValue() ).getMessageValue() )
        {
            // A new shortest path to me was found
            // aggregate number of messages sent in this step & in this problem
            aggregateOutputProblemAggregator( new IntegerSumAggregator( getOutEdgeMapSize() ));
            aggregateOutputStepAggregator(    new IntegerSumAggregator( getOutEdgeMapSize() ));
            
            // there is a new shorter path from the source to me
            setVertexValue( minDistanceMessage ); // update my value: the shortest path to me
            
            // To each target vertex: The shortest known path to you through me just got shorter 
            for ( Point2D.Float targetVertexId : getOutEdgeValues() )            
            {
                float edgeValue = distance( targetVertexId );
                Message<Float> message = new Message<Float>( getVertexId(), minDistanceMessage.getMessageValue() + edgeValue );   
                sendMessage( targetVertexId, message );
            }
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
        stringBuffer.append( (Point2D.Float) getVertexId() );
        stringBuffer.append( " : ");
        stringBuffer.append( (Point2D.Float) ((Message)  getVertexValue() ).getVertexId() );
        stringBuffer.append( " --- ");
        stringBuffer.append( (Float) ((Message)  getVertexValue() ).getMessageValue() );
        return new String( stringBuffer );
    }
    
    /*
     * Would prefer to map square subgrids to a part.
     */
    public int getPartId( Object vertexId, int numParts )
    {
        int row = (int) ((Point2D.Float) vertexId).getX();
        return row % numParts;
    }
    
    protected boolean isSource()
    {
        Point2D.Float vertex = (Point2D.Float) getVertexId();
        return vertex.getX() == 0.0 && vertex.getY() == 0.0; 
    }
    
    private float distance( Point2D.Float targetVertexId )
    {
        double x1 = ((Point2D.Float) getVertexId()).getX();
        double y1 = ((Point2D.Float) getVertexId()).getY();
        double x2 = targetVertexId.getX();
        double y2 = targetVertexId.getY();
        double deltaX = x1 - x2;
        double deltaY = y1 - y2;
        return (float) sqrt( deltaX * deltaX + deltaY * deltaY );
    }
}
