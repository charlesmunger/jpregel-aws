package workerGraphMakers;

import static java.lang.System.err;
import static java.lang.System.exit;

import JpAws.WorkerGraphFileIO;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import system.*;
import vertex.BinaryTreeShortestPathVertex;

/**
 *
 * @author Pete Cappello
 */
public class BinaryTreeWorkerGraphMaker implements GraphMaker
{
    // TODO WorkerGraphMakers: Perhaps multithreading would speed them up.
    @Override
    public int makeGraph(Worker worker) 
    {
        int workerNum         = worker.getWorkerNum();
        Job job               = worker.getJob();
        FileSystem fileSystem = job.getFileSystem();
        Vertex vertexFactory  = job.getVertexFactory();
        int numVertices = 0;
        
        FileInputStream fileInputStream = null;
        DataInputStream dataInputStream = null;
        BufferedReader bufferedReader   = null;
        
        try
        {
            boolean isEc2 = fileSystem.getFileSystem() ; 
            if (isEc2) 
            {
                String jobDirectoryName = fileSystem.getJobDirectory() ; 
                WorkerGraphFileIO workerGraph = new WorkerGraphFileIO(workerNum) ; 
                bufferedReader = workerGraph.FileInput(jobDirectoryName) ; 
                //bufferedReader = S3FileSystem.WorkerFileInput(jobDirectoryName, workerNum) ;       
            }
            else 
            {   // open file locally
                fileInputStream = fileSystem.getWorkerInputFileInputStream( workerNum );
                dataInputStream = new DataInputStream( fileInputStream );
                bufferedReader   = new BufferedReader(new InputStreamReader( dataInputStream ));  
            }
            
            // read file
            String line = bufferedReader.readLine();
            bufferedReader.close();
            if ( ! isEc2 )
            { 
                dataInputStream.close();
                fileInputStream.close(); 
            } 
            
            // extract startVertexId, stopVertexId
            StringTokenizer stringTokenizer = new StringTokenizer( line );
            int startVertexId = getToken( stringTokenizer );
            int stopVertexId  = getToken( stringTokenizer );
            numVertices       = getToken( stringTokenizer );
            System.out.println("BinaryTreeWorkerGraphMaker.make: workerNum: " + workerNum 
                    + ", startVertexId: " + startVertexId 
                    + ", stopVertexId: "  + stopVertexId  + ", numVertices: " + numVertices );
                        
            // construct vertices
            for ( int vertexId = startVertexId; vertexId <= stopVertexId; vertexId++ )
            {
                StringBuilder stringVertex = new StringBuilder();
                stringVertex.append( vertexId ).append(' ').append( numVertices );
                
                // initialize vertexValue
                Integer initialKnownDistance = ( vertexId == 1 ) ? 0 : Integer.MAX_VALUE;
                Message<Integer, Integer> minDistanceMessage = new Message<Integer, Integer>( 0, initialKnownDistance );
                
                // initiaize out edges (children)
                Map<Integer, Message<Integer, Integer>> outEdgeMap = new HashMap<Integer, Message<Integer, Integer>>( 2 );
                Integer targetVertexId = 2 * vertexId;
                if ( targetVertexId <= numVertices )
                {   // make OutEdge for left child
                    Message<Integer, Integer> outEdge = new Message<Integer, Integer>( targetVertexId, 1 );
                    outEdgeMap.put( targetVertexId, outEdge );

                    if ( targetVertexId < numVertices )
                    {   // make OutEdge for right child
                        outEdge = new Message<Integer, Integer>( ++targetVertexId, 1 );
                        outEdgeMap.put( targetVertexId, outEdge );
                    }
                }
                
                Vertex vertex = new BinaryTreeShortestPathVertex( vertexId, minDistanceMessage, outEdgeMap );
                String vertexString = new String( stringVertex );
                worker.addVertex( vertex, vertexString );
            }
        }
        catch ( Exception exception )
        {
            err.println( "GridWorkerGraphMaker.makeGraph: Error: " + exception.getMessage());
            exception.printStackTrace();
            exit( 1 );
        }
        return numVertices;
    }
    
    private int getToken( StringTokenizer stringTokenizer ) throws IOException
    {
        if ( ! stringTokenizer.hasMoreTokens() )
        {
            err.println( "GridWorkerGraphMaker.makeGraph: getToken: Empty lines are not allowed." );
            throw new IOException();
        }
        return Integer.parseInt( stringTokenizer.nextToken() );
    }
}
