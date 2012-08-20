package system;

import static java.lang.System.err;
import static java.lang.System.exit;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

public class VertexPageRank extends VertexImpl<Integer, Double, OutEdge, Double> 
{
    public static Combiner combiner = new CombinerMinInteger(); //TODO hides a field
    
    public VertexPageRank( Integer vertexId, Map<Integer, OutEdge> outEdgeMap )
    {
        super( vertexId, outEdgeMap );
        setVertexValue( Double.MAX_VALUE  );
    }
	
    public VertexPageRank() {}
	
    @Override
    public void compute() 
    {
        double sum = 0 ; 
        if ( getSuperStep() >= 1 ) 
        {
            Iterator<Message<Integer, Double>> messageSetIterator = getMessageIterator();

            while ( messageSetIterator.hasNext() )
            {
                Message<?, Double> message = messageSetIterator.next();
                sum += message.getMessageValue() ;   
            }
            setVertexValue( 0.15/ getNumVertices() + 0.85 * sum ) ; 
        } 
        if (getSuperStep() < 30) 
        { 
            double messageValue = getVertexValue() / (getNumVertices() - getVertexId() - 1 ) ; 
            for(int targetVertexId = getVertexId() + 1 ; targetVertexId <= getNumVertices() ; targetVertexId++ )
            {
                    Message<Integer, Double> message = new Message<Integer, Double>( getVertexId(), messageValue );
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
//                voteToHalt() ; 
        }
    }

	@Override
	public String output() 
        {
            StringBuilder string = new StringBuilder();
            string.append( getVertexId() );
            string.append( " : ");
//            string.append( ((Message)  getVertexValue() ).getVertexId() );
//            string.append( " ");
//            string.append( ((Message) getVertexValue() ).getMessageValue() );
            string.append( getVertexValue() );
            return new String( string );
	}

	@Override
        public VertexImpl make(String line)
        {
            StringTokenizer stringTokenizer = new StringTokenizer( line );
            if ( ! stringTokenizer.hasMoreTokens() )
            {
                err.println( "PageRankVertex.make: Empty lines are not allowed." );
                exit( 1 );
            }
            int vertexId = Integer.parseInt( stringTokenizer.nextToken() );
            setVertexValue( 1.0 / getNumVertices() ) ; 
            int endVertexValue = Integer.parseInt( stringTokenizer.nextToken() ); 
            int numVertices  = Integer.parseInt( stringTokenizer.nextToken() ); //TODO hides a field

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

            Map<Integer, OutEdge> outEdgeMap = new HashMap<Integer, OutEdge>(); 
            /*while( stringTokenizer.hasMoreTokens() )
            {
                    int target = Integer.parseInt( stringTokenizer.nextToken() );
                int weight = Integer.parseInt( stringTokenizer.nextToken() ); 
                outEdgeMap.put( target, new OutEdge( target, weight ) );
            } */

            return new VertexPageRank( vertexId, outEdgeMap );			
	}
        
        /**
         * Unused method
         */
        @Override
        public boolean isSource() { return false; }
}
