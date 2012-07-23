package clients;

import java.rmi.RemoteException;
import masterGraphMakers.GridMasterGraphMaker;
import masterOutputMakers.StandardMasterOutputMaker;
import system.*;
import system.aggregators.IntegerSumAggregator;
import system.combiners.FloatMinCombiner;
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
    public static void main( String[] args ) throws RemoteException
    {
        int numWorkers = 2 * 2; // should be n * n, for some natural number n
        int computeThreadsPerWorker = 2;
        int partsPerComputeThread = 2;
        int numParts = numWorkers * computeThreadsPerWorker * partsPerComputeThread;        
        Job job = new Job(
                "Euclidean 2D Grid Shortest Path", // jobName
                args[0],                           // jobDirectoryName, 
                new EuclideanShortestPathVertex(), // vertexFactory, 
                numParts, 
                true,                              // workerIsMultithreaded, 
                new FloatMinCombiner(),            // Combiner
                new StandardWorkerOutputMaker(),   // WorkerWriter, 
                new GridWorkerGraphMaker(),        // WorkerGraphMaker, 
                new GridMasterGraphMaker(),        // MasterGraphMaker
                new StandardMasterOutputMaker()    // Writer 
                );
        job.setProblemAggregator( new IntegerSumAggregator() );
        System.out.println( "Euclidean2DGridShortestPathMacClient: \n  numWorkers: " 
                + numWorkers + "\n  numParts: " + numParts + "\n  " + job );
        try
        {
            boolean   isEc2Master = false;
            Client.run( job, isEc2Master, numWorkers); //TODO fix this
        } 
        catch ( Exception exception )
        {
            exception.printStackTrace();
            System.exit(1);
        }
        System.exit( 0 );
    }
}
