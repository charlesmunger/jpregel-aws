package system;

import api.WorkerGraphMaker;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import vertices.VertexShortestPathBinaryTree;

/**
 *
 * @author Pete Cappello
 */
public class WorkerGraphMakerBinaryTree implements WorkerGraphMaker
{
    @Override
    public int makeGraph(Worker worker) 
    {
        int workerNum         = worker.getWorkerNum();
        Job job               = worker.getJob();
        FileSystem fileSystem = job.getFileSystem();
        int numVertices = 0;
        
        try
        {
            BufferedReader bufferedReader = fileSystem.getWorkerInputFileInputStream( workerNum );
            
            // read file
            String line = bufferedReader.readLine();
            bufferedReader.close();
            
            // extract startVertexId, stopVertexId
            StringTokenizer stringTokenizer = new StringTokenizer( line );
            int startVertexId = getToken( stringTokenizer );
            int stopVertexId  = getToken( stringTokenizer );
            numVertices       = getToken( stringTokenizer );
            System.out.println("BinaryTreeWorkerGraphMaker.make: workerNum: " + workerNum 
                    + ", startVertexId: " + startVertexId 
                    + ", stopVertexId: "  + stopVertexId  + ", numVertices: " + numVertices );
                        
            // construct vertices
            VertexShortestPathBinaryTree vertexMaker = new VertexShortestPathBinaryTree();
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
            ExecutorService exec = Executors.newFixedThreadPool( Runtime.getRuntime().availableProcessors() );
            makeNodes(exec,startVertexId, (numVertices - 1)/2, 2, vertexMaker, job, worker, workerNum );
            if ( numVertices % 2 == 0)
            {
                //makeNodes(exec,numVertices/2, numVertices/2, 1, vertexMaker, job, worker, workerNum );
                makeNodes(exec,numVertices/2, numVertices/2, 1, vertexMaker, job, worker, workerNum );
            }
            makeNodes(exec,numVertices/2 + 1, stopVertexId, 0, vertexMaker, job, worker, workerNum );
            exec.shutdown();
            exec.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        }
        catch ( Exception exception )
        {
            System.err.println( "GridWorkerGraphMaker.makeGraph: Error: " + exception.getMessage());
            exception.printStackTrace();
            System.exit( 1 );
        }
        return numVertices;
    }
    
    private int getToken( StringTokenizer stringTokenizer ) throws IOException
    {
        if ( ! stringTokenizer.hasMoreTokens() )
        {
            System.err.println( "GridWorkerGraphMaker.makeGraph: getToken: Empty lines are not allowed." );
            throw new IOException();
        }
        return Integer.parseInt( stringTokenizer.nextToken() );
    }
    
    private class NodeMaker implements Callable<Object>
    {
        private final int startVertexId;
        private final int stopVertexId;
        private final int numChildren;
        private final VertexShortestPathBinaryTree vertexFactory;
        private final Job job;
        private final Worker worker;
        private final int workerNum;

        public NodeMaker(int startVertexId, int stopVertexId, int numChildren, VertexShortestPathBinaryTree vertexFactory, Job job, Worker worker, int workerNum)
        {
            this.startVertexId = startVertexId;
            this.stopVertexId = stopVertexId;
            this.numChildren = numChildren;
            this.vertexFactory = vertexFactory;
            this.job = job;
            this.worker = worker;
            this.workerNum = workerNum;
        }

        @Override
        public Object call() throws Exception
        {
            makeNodes(startVertexId, stopVertexId, numChildren, vertexFactory, job, worker, workerNum);
            return null;
        }
    }
    
    private void makeNodes(Integer startVertexId, Integer stopVertexId, Integer numChildren, VertexShortestPathBinaryTree vertexFactory, Job job, Worker worker, Integer workerNum)
    {
        int numParts = job.getNumParts();
        for ( int vertexId = startVertexId; vertexId <= stopVertexId; vertexId++ )
        {
            int partId = vertexFactory.getPartId( vertexId, numParts );
            int destinationWorkerNum = worker.getWorkerNum( partId );
            if ( destinationWorkerNum == workerNum )
            {   // vertex belongs to this worker
                VertexImpl vertex = vertexFactory.make( vertexId, numChildren );
                worker.addVertexToPart(partId, vertex);
            } else
            {   // vertex belongs to another worker
                StringBuilder stringVertex = new StringBuilder();
                stringVertex.append(vertexId).append(" ").append(numChildren);
                worker.addRemoteVertex(destinationWorkerNum, partId, new String(stringVertex) );
            }
        }
    }
    
    private void makeNodes( ExecutorService exec, int startVertexId, int stopVertexId, int numChildren,
            VertexShortestPathBinaryTree vertexFactory, Job job, Worker worker,
            int workerNum)
    {
        int naturalChunkSize = ( stopVertexId - startVertexId + 1 ) / ( Runtime.getRuntime().availableProcessors() * 2 );
        final int chunkSize = Math.max( 1024 * 64, naturalChunkSize ); // 500000;
        for (int i = startVertexId; i <= stopVertexId; i += chunkSize)
        {
            exec.submit( new NodeMaker( i, Math.min( i + chunkSize, stopVertexId ),
                         numChildren, vertexFactory, job, worker, workerNum ) ) ;
        }
    }
}
