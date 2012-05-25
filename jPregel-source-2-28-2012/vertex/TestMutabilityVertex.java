package vertex;

import static java.lang.System.err;
import static java.lang.System.exit;
import static java.lang.System.out;


import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import system.Combiner;
import system.Message;
import system.OutEdge;
import system.Vertex;

/**
 *
 * @author Pete Cappello
 */
public class TestMutabilityVertex extends Vertex<OutEdge, Integer>
{
    public TestMutabilityVertex( Integer vertexId, Map<Object, OutEdge> outEdgeMap, Combiner<Integer> combiner )
    {
        super( vertexId, outEdgeMap, combiner );
        setVertexValue( new Message<Integer>( vertexId, Integer.MAX_VALUE ) );
    }
    
    public TestMutabilityVertex() {}
    
    public Vertex make( String line, Combiner combiner )
    {
        StringTokenizer stringTokenizer = new StringTokenizer( line );
        if ( ! stringTokenizer.hasMoreTokens() )
        {
            err.println( "TestMutabilityVertex.make: Empty lines are not allowed." );
            exit( 1 );
        }
        int vertexId = Integer.parseInt( stringTokenizer.nextToken() );
        Map<Object, OutEdge> outEdgeMap = new HashMap<Object, OutEdge>();
        while( stringTokenizer.hasMoreTokens() )
        { 
            int target  = Integer.parseInt( stringTokenizer.nextToken() );
            int weight  = Integer.parseInt( stringTokenizer.nextToken() ); 
            outEdgeMap.put( target, new OutEdge( target, weight ) );
        }
        return new TestMutabilityVertex( vertexId, outEdgeMap, combiner );
    }
    
    @Override
    public void compute() 
    {
        if ( getSuperStep() == 0 && (Integer) getVertexId() == 0 )
        {
            addEdge( new Integer( 2 ), new OutEdge( 2, -1 ) );
        }
        if ( getSuperStep() == 1 && (Integer) getVertexId() == 0 )
        {
            removeEdge( new Integer( 1 ) );
        }
        if ( getSuperStep() == 2 && (Integer) getVertexId() == 1 )
        {
            out.println("TestMutabilityVertex.compute: step: " + getSuperStep() + " numVertices: " + getNumVertices() );
            removeVertex();
        }
        if ( getSuperStep() == 3 && (Integer) getVertexId() == 0 )
        {
            out.println("TestMutabilityVertex.compute: step: numVertices: " + getNumVertices() );
        }
        
        /* This vote will be overturned, if during this step, a vertex for whom 
         * I am a target vertex discovered a shorter path to itself, 
         * in which case, it will send me a message.
         */   
        if ( getSuperStep() == 3 )
        {
            voteToHalt(); 
        }
    }

    @Override
    public String output() 
    {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append( getVertexId() );
        stringBuffer.append( " : ");
//        stringBuffer.append( ((Message)  getVertexValue() ).getMessageValue() );
        for ( OutEdge outEdge : getOutEdgeValues() )
            {
                stringBuffer.append( (Integer) outEdge.getVertexId() );
                stringBuffer.append( " ");
                stringBuffer.append( (Integer) outEdge.getEdgeValue() );
                stringBuffer.append( " ");
            }
        return new String( stringBuffer );
    }
    
    boolean isSource() { return ( (Integer) getVertexId() == 0 ) ? true : false; }
}