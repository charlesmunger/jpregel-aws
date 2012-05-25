package vertex;

import static java.lang.System.err;
import static java.lang.System.exit;
import static java.lang.System.out;

import static java.lang.Math.sqrt;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import system.Combiner;
import system.Message;
import system.MessageQ;
import system.OutEdge;
import system.Vertex;
import system.aggregators.IntegerSumAggregator;

public class PageRankVertex extends Vertex<OutEdge, Double> {

	
	public PageRankVertex( Integer vertexId, Map<Object, OutEdge> outEdgeMap, Combiner<Integer> combiner )
    {
        super( vertexId, outEdgeMap, combiner );
        setVertexValue( new Message( vertexId, Integer.MAX_VALUE ) );
    }
	
    public PageRankVertex() {}

	
	@Override
	protected void compute() {
		double sum = 0 ; 
		if ( getSuperStep() >= 1 ) 
		{
        Iterator<Message<Double>> messageSetIterator = getMessageIterator();
        
        while ( messageSetIterator.hasNext() )
        {
            Message<Double> message = messageSetIterator.next();
            sum += message.getMessageValue() ;   
        }
        setVertexValue( 0.15/ getNumVertices() + 0.85 * sum ) ; 
		} 
		if (getSuperStep() < 30) 
		{ 
			double messageValue = (Double) getVertexValue() / (getNumVertices() - (Integer)getVertexId() - 1 ) ; 
			for(int targetVertexId = (Integer)(getVertexId()) + 1 ; targetVertexId <= getNumVertices() ; targetVertexId++ )
			{
				Message<Double> message = new Message<Double>( getVertexId(), messageValue );
				sendMessage( targetVertexId , message) ;  
			}
			//final long n  = getOutEdgeMapSize() ;  
			//OutEdge outEdge  = null ; 
            //Object targetVertexId = outEdge.getVertexId();
            //Message<Double> message = new Message<Double>( getVertexId(), messageValue );
			//sendMessage( targetVertexId , message) ;  
		} 
		else 
		{ 
			voteToHalt() ; 
		}
		
	}

	@Override
	public String output() {

		StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append( getVertexId() );
        stringBuffer.append( " : ");
        stringBuffer.append( ((Message)  getVertexValue() ).getVertexId() );
        stringBuffer.append( " ");
        stringBuffer.append( ((Message)  getVertexValue() ).getMessageValue() );
        return new String( stringBuffer );
	
	}

	@Override
	public Vertex make(String line, Combiner combiner) {

		StringTokenizer stringTokenizer = new StringTokenizer( line );
        if ( ! stringTokenizer.hasMoreTokens() )
        {
            err.println( "ShortestPathVertex.make: Empty lines are not allowed." );
            exit( 1 );
        }
        int vertexId = Integer.parseInt( stringTokenizer.nextToken() );
        setVertexValue(1/ getNumVertices()) ; 
        int endVertexValue = Integer.parseInt( stringTokenizer.nextToken() ); 
        int numVertices  = Integer.parseInt( stringTokenizer.nextToken() ); 
        
        while(vertexId <= endVertexValue)
        {
         
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append( vertexId ).append( ' ' );	
        for(int vertexIdentifier = vertexId ; vertexIdentifier < numVertices ; vertexIdentifier++ )
        {
        	stringBuffer.append( vertexIdentifier).append( ' ' ); 
        	
        }
        String lines = new String( stringBuffer );
        vertexId++ ; 
        }
        
        Map<Object, OutEdge> outEdgeMap = new HashMap<Object, OutEdge>(); 
        /*while( stringTokenizer.hasMoreTokens() )
        {
        	 int target = Integer.parseInt( stringTokenizer.nextToken() );
             int weight = Integer.parseInt( stringTokenizer.nextToken() ); 
             outEdgeMap.put( target, new OutEdge( target, weight ) );
        } */
		
        return new PageRankVertex( vertexId, outEdgeMap, combiner );			
	}

}
