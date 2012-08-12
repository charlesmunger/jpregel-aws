package clients;

import system.MasterGraphMakerBinaryTree;
import system.MasterOutputMakerStandard;
import system.Client;
import system.Job;
import system.VertexShortestPathBinaryTree;
import system.WorkerGraphMakerBinaryTree;
import system.WorkerOutputMakerStandard;

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
        int numWorkers = Integer.parseInt(args[1]); 
        int computeThreadsPerWorker = Runtime.getRuntime().availableProcessors();
        int partsPerComputeThread = 2;
        int numParts = numWorkers * computeThreadsPerWorker * partsPerComputeThread;        
        Job job = new Job(
                "Binary Tree Shortest Path",        // jobName
                args[0],                            // jobDirectoryName
                new VertexShortestPathBinaryTree(), // vertexFactory
                numParts, 
                new WorkerOutputMakerStandard(),    // WorkerWriter
                new WorkerGraphMakerBinaryTree(),   // WorkerGraphMaker
                new MasterGraphMakerBinaryTree(),   // MasterGraphMaker
                new MasterOutputMakerStandard()     // Writer 
                );
//        System.out.println("JVM data model: " + System.getProperty("sun.arch.data.model"));
        System.out.println( job + "\n  numWorkers: " + numWorkers );
        boolean isEc2Master = false;
        Client.run( job, isEc2Master, numWorkers);
        System.exit( 0 );
    }
}
