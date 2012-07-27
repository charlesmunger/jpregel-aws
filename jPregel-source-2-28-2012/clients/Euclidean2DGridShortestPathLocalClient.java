package clients;

import masterGraphMakers.GridMasterGraphMaker;
import masterOutputMakers.StandardMasterOutputMaker;
import system.Client;
import system.Job;
import system.aggregators.IntegerSumAggregator;
import vertex.EuclideanShortestPathVertex;
import workerGraphMakers.GridWorkerGraphMaker;
import workerOutputMakers.StandardWorkerOutputMaker;

/**
 *
 * @author Pete Cappello
 */
public class Euclidean2DGridShortestPathLocalClient 
{
    /**
     * @param args [0]: Job directory name
     */
    public static void main( String[] args ) throws Exception
    {
        int numWorkers = 2 * 2; // should be n * n, for some natural number n
        int computeThreadsPerWorker = Runtime.getRuntime().availableProcessors();
        int partsPerComputeThread = 2;
        int numParts = numWorkers * computeThreadsPerWorker * partsPerComputeThread;        
        Job job = new Job(
                "Euclidean 2D Grid Shortest Path", // jobName
                args[0],                           // jobDirectoryName, 
                new EuclideanShortestPathVertex(), // vertexFactory, 
                numParts, 
                true,                              // workerIsMultithreaded, 
                new StandardWorkerOutputMaker(),   // WorkerWriter, 
                new GridWorkerGraphMaker(),        // WorkerGraphMaker, 
                new GridMasterGraphMaker(),        // MasterGraphMaker
                new StandardMasterOutputMaker()    // Writer 
                );
        job.setProblemAggregator( new IntegerSumAggregator() );
        System.out.println( "Euclidean2DGridShortestPathMacClient: \n  numWorkers: " 
                + numWorkers + "\n  numParts: " + numParts + "\n  " + job );
        boolean isEc2Master = false;
        Client.run( job, isEc2Master, numWorkers);
        System.exit( 0 );
    }
}
