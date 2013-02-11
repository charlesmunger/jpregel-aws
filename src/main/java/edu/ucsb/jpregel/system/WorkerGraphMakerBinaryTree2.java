package edu.ucsb.jpregel.system;

import api.WorkerGraphMaker;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import util.Log2;
import vertices.VertexShortestPathBinaryTree;

/**
 * Constructs the nodes associated with a particular Worker.
 * 
 * @author Pete Cappello
 */
public class WorkerGraphMakerBinaryTree2 extends WorkerGraphMaker
{
    private final VertexShortestPathBinaryTree vertexFactory = new VertexShortestPathBinaryTree();
    private int numParts;
    private int numVertices;
    private int numPartsPerWorker;
    
    // variable cache
    private int lgNumParts;
    private int treeHeight;
    private int partTreeHeight;
    private int numPartTreeFullDepthLeaves;
    
    @Override
    public int makeGraph(Worker worker) 
    {
        int numVerticesMade = 0;
        Job job = worker.getJob();
        FileSystem fileSystem = job.getFileSystem();
        try
        {
            BufferedReader bufferedReader = fileSystem.getWorkerInputFileInputStream( worker.getWorkerNum() );
            
            // read file
            String line = bufferedReader.readLine();
            bufferedReader.close();
            
            // extract numVertices, numWorkers
            StringTokenizer stringTokenizer = new StringTokenizer( line );
            numVertices = getToken( stringTokenizer );
            int numWorkers = getToken( stringTokenizer );
            int numCores = Runtime.getRuntime().availableProcessors();
            numPartsPerWorker = 2 * numCores;
            treeHeight = Log2.lg( numVertices );
            numParts = numWorkers * numPartsPerWorker;
            lgNumParts = Log2.lg( numParts );
            partTreeHeight = treeHeight - lgNumParts;
            numPartTreeFullDepthLeaves = 1 << partTreeHeight;
//            System.out.println("WorkerGraphMakerBinaryTree2.make: workerNum: " + worker.getWorkerNum() +
//                    "  numVertices: " + numVertices + " treeHeight: " + treeHeight +
//                    " numParts: " + numParts + " lgNumParts: " + lgNumParts + " partTreeHeight: " + partTreeHeight
//                    + " numPartTreeFullDepthLeaves: " + numPartTreeFullDepthLeaves);
            
            // determine my partitions
            int myStartPartId = (worker.getWorkerNum() - 1) * numPartsPerWorker;
            int myStopPartId = myStartPartId + numPartsPerWorker - 1;
//            System.out.println("WorkerGraphMakerBinaryTree2.make: workerNum: " + worker.getWorkerNum()
//                    + ", numVertices: " + numVertices 
//                    + ", numWorkers: "  + numWorkers  + ", numCores: " + numCores 
//                    + ", numParts: " + numParts
//                    + ", myStartPart: " + myStartPartId + ", myStopPart: " + myStopPartId);
            
            // for each of my partitions, p, produce p's vertices.
            ExecutorService executorService = Executors.newFixedThreadPool( numCores );
//            System.out.println("make: workerNum: " + worker.getWorkerNum() + "  myStartPartId: " + myStartPartId);
            for ( int partId = myStartPartId; partId <= myStopPartId; partId++ )
            {
                int partSize = getPartSize( partId );
//                System.out.println("WorkerGraphMakerBinaryTree2.makeGraph: partId: " + partId
//                        + " partSize: " + partSize);
                numVerticesMade += partSize;
                Callable<Object> task = new PopulatePart( worker, partId, partSize );
                executorService.submit( task );
            }            
            executorService.shutdown();
            executorService.awaitTermination( Long.MAX_VALUE, TimeUnit.DAYS );
        }
        catch ( Exception exception )
        {
            System.err.println( "GridWorkerGraphMaker.makeGraph: Error: " + exception.getMessage());
            exception.printStackTrace();
            System.exit( 1 );
        }
        return numVerticesMade;
    }
    
    @Override
    public int getWorkerNum( int partId, int numWorkers )
    {
        return partId / numPartsPerWorker + 1;
    }
    
    private int getPartSize( int partId )
    {
//        System.out.println("getPartSize: partId: " + partId +
//                "  taprootSize: " + getTaprootSize( partId ) +
//                "  numPartTreeFullDepthLeaves - 1: " + (numPartTreeFullDepthLeaves - 1) +
//                "  numPartFullDepthLeaves: " + numPartFullDepthLeaves( partId ) );
        return getTaprootSize( partId ) + numPartTreeFullDepthLeaves - 1 + numPartFullDepthLeaves( partId );
    }
    
    private int numPartFullDepthLeaves( int partId )
    {
        int leftFullDepthLeafId = ( numParts + partId ) << partTreeHeight;
//        System.out.print("    numPartFullDepthLeaves: partId: " + partId + "  leftFullDepthLeafId: " + leftFullDepthLeafId);
        if ( numVertices < leftFullDepthLeafId )
        {
            return 0;
        }
        if ( numVertices >= leftFullDepthLeafId + numPartTreeFullDepthLeaves )
        {
            return numPartTreeFullDepthLeaves;
        }
        return numVertices - leftFullDepthLeafId + 1;
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

    private int getTaprootSize( int partNum )
    {
        return ( partNum == 0 ) ? lgNumParts : Integer.numberOfTrailingZeros( partNum );
    }
    
    private class PopulatePart implements Callable<Object>
    {
        private final Worker worker;
        private final int partId;
        private final int partSize;

        public PopulatePart( Worker worker, int partId, int partSize )
        {
            this.worker = worker;
            this.partId = partId;
            this.partSize = partSize;
        }

        @Override
        public Object call()
        {
//            System.out.println("PopulatePart: workerNum: " + worker.getWorkerNum() + "  partId: " + partId + "  partSize: " + partSize );
            
            int numVerticesMade = 0;
            
            // make taproot vertices
            int vertexId = numParts + partId;
            VertexImpl vertex;
            while ( Integer.numberOfTrailingZeros( vertexId ) > 0 )
            {
//                System.out.println("Populate.call:  taproot partId: " + partId + "  vertexId: " + vertexId);
                vertex = vertexFactory.make( vertexId, 2 );
                worker.addVertexToPart(partId, vertex);
                numVerticesMade++;
                vertexId /= 2;
            }
//            System.out.println("Populate.call:   LAST taproot partId: " + partId + "  vertexId: " + vertexId);
            vertex = vertexFactory.make( vertexId, 2 );
            worker.addVertexToPart(partId, vertex);
            numVerticesMade++;
            
            // make partTree vertices
            LinkedList<Integer> q = new LinkedList<Integer>();
            int partTreeRootVertexId = numParts + partId; // vertex has been made
            q.add( 2 * partTreeRootVertexId );
            q.add( 2 * partTreeRootVertexId + 1 );
            for ( ; numVerticesMade < partSize; numVerticesMade++ )
            {
                vertexId = q.removeLast();
                int numChildren = 0;
                if ( 2 * vertexId <= numVertices )
                {
                    numChildren++;
                    q.add( 2 * vertexId );
                    if ( 2 * vertexId + 1 <= numVertices )
                    {
                        numChildren++;
                        q.add( 2 * vertexId + 1 );
                    }
                }
                vertex = vertexFactory.make( vertexId, numChildren );
                worker.addVertexToPart(partId, vertex);
//                System.out.println("PopulatePart.call: partId: " + partId + " numVerticesMade: " + numVerticesMade + " vertexId: " + vertexId);
            }
            
            // BEGIN DEBUG
            while ( q.size() > 0 )
            {
                System.out.println(" IN FLUSH LOOP: vertexId: " + q.removeLast() + "  partSize: " + partSize);
            }
            // END DEBUG
            assert q.isEmpty();
            return null;
        }
    }
}
