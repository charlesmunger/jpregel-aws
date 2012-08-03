package clients;

import masterGraphMakers.BinaryTreeMasterGraphMaker;
import masterOutputMakers.StandardMasterOutputMaker;
import system.Client;
import system.Job;
import system.NullAggregator;
import vertex.BinaryTreeShortestPathVertex;
import workerGraphMakers.BinaryTreeWorkerGraphMaker;
import workerOutputMakers.StandardWorkerOutputMaker;

/**
 *
 * @author Pete Cappello
 */
public class BinaryTreeShortestPathLocalClient 
{  
    /**
     * @param args [0]: Job directory name
     */
    public static void main( String[] args ) throws Exception
    {
        int numWorkers = 2; 
        int computeThreadsPerWorker = Runtime.getRuntime().availableProcessors();
        int partsPerComputeThread = 2;
        int numParts = numWorkers * computeThreadsPerWorker * partsPerComputeThread;        
        Job job = new Job(
                "Binary Tree Shortest Path",        // jobName
                args[0],                            // jobDirectoryName
                new BinaryTreeShortestPathVertex(), // vertexFactory
                numParts, 
                new StandardWorkerOutputMaker(),    // WorkerWriter
                new BinaryTreeWorkerGraphMaker(),   // WorkerGraphMaker
                new BinaryTreeMasterGraphMaker(),   // MasterGraphMaker
                new StandardMasterOutputMaker()     // Writer 
                );
//        System.out.println("JVM data model: " + System.getProperty("sun.arch.data.model"));
        job.setProblemAggregator( new NullAggregator() );
        System.out.println( job + "\n  numWorkers: " + numWorkers );
        boolean isEc2Master = false;
        Client.run( job, isEc2Master, numWorkers);
        System.exit( 0 );
    }
}
