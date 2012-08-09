package workerGraphMakers;

import JpAws.WorkerGraphFileIO;
import java.io.*;
import static java.lang.System.err;
import static java.lang.System.exit;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import system.FileSystem;
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
                bufferedReader  = new BufferedReader(new InputStreamReader( dataInputStream ));  
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
            BinaryTreeShortestPathVertex vertexMaker = new BinaryTreeShortestPathVertex();
            //TODO If we were to do this in parallel, we should make a ThreadPoolExecutorService.
            /*
             * Let the binary tree have nodes numbered 1 to N, where 1 is
             * the root, and if the node numbered n has 2 children, 
             * the left  child is numbered 2n;
             * the right child is numbered 2n + 1.
             * Then, node n has:
             *  2 children,  when 1 <= n <= (N - 1)/2;
             *  1 child (a left child), when n is even and n = N/2;
             *  0 children, otherwise (i.e., (N + 1)/2 <= n <= N).
             */
            makeNodes(startVertexId, (numVertices - 1)/2, 2, vertexMaker, job, worker, workerNum );
            if ( numVertices % 2 == 0)
            {
                makeNodes(numVertices/2, numVertices/2, 1, vertexMaker, job, worker, workerNum );
            }
            makeNodes(numVertices/2 + 1, stopVertexId, 0, vertexMaker, job, worker, workerNum );
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
    
    private void makeNodes(int startVertexId, int stopVertexId, int numChildren, 
            BinaryTreeShortestPathVertex vertexFactory, Job job, Worker worker,
            int workerNum)
    {
        for ( int vertexId = startVertexId; vertexId <= stopVertexId; vertexId++ )
        {
            int partId = vertexFactory.getPartId( vertexId, job.getNumParts() );
            int destinationWorkerNum = worker.getWorkerNum( partId );
            if ( destinationWorkerNum == workerNum )
            {   // vertex belongs to this worker
                Vertex vertex = vertexFactory.make( vertexId, numChildren );
                worker.addVertexToPart(partId, vertex);
            } else
            {   // vertex belongs to another worker
                StringBuilder stringVertex = new StringBuilder();
                stringVertex.append(vertexId).append(" ").append(numChildren);
                worker.addRemoteVertex(destinationWorkerNum, partId, new String(stringVertex) );
            }
        }
    }
}
