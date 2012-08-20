package clients;

import system.MasterGraphMakerGrid;
import system.MasterOutputMakerStandard;
import system.Client;
import system.Job;
import system.AggregatorSumInteger;
import system.VertexShortestPathEuclidean;
import system.WorkerGraphMakerGrid;
import system.WorkerOutputMakerStandard;

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
                new VertexShortestPathEuclidean(), // vertexFactory, 
                numParts,
                new MasterGraphMakerGrid(),
                new WorkerGraphMakerGrid(),
                new MasterOutputMakerStandard(),
                new WorkerOutputMakerStandard(),
                new AggregatorSumInteger(),   // problem aggregator
                null                          // step    agregator
                );
        System.out.println( job +  "\n  numParts: " + numParts );
        boolean isEc2Master = false;
        Client.run( job, isEc2Master, numWorkers);
        System.exit( 0 );
    }
}
