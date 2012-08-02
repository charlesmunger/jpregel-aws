package clients;

import java.rmi.RemoteException;
import vertex.EuclideanShortestPathVertex;
import masterGraphMakers.StandardMasterGraphMaker;
import masterOutputMakers.StandardMasterOutputMaker;
import system.Client;
import system.Combiner;
import system.Job;
import system.aggregators.IntegerSumAggregator;
import system.combiners.FloatMinCombiner;
import workerGraphMakers.StandardWorkerGraphMaker;
import workerOutputMakers.StandardWorkerOutputMaker;

/**
 *
 * @author Pete Cappello
 */
public class EuclideanShortestPathLocalClient 
{
    /**
     * @param args [0]: Job Directory
     *             [1]: Number of Workers
     *             [2]: true if and only if worker is to be multi-threaded
     */
    public static void main( String[] args ) throws Exception
    {
        int     numWorkers              = Integer.parseInt(     args[1] );
        int     computeThreadsPerWorker = Runtime.getRuntime().availableProcessors();
        int     partsPerComputeThread   = 2;
        int     numParts                = numWorkers * computeThreadsPerWorker * partsPerComputeThread;
        boolean isEc2Run = false;
        System.out.println("EuclideanShortestPathLocalClient: numWorkers: " + numWorkers + " isAWS run: " + isEc2Run);
        
        Job job = new Job( 
                "Euclidean Shortest Path",            // Job name
                args[0],                              // Job directory name
                new EuclideanShortestPathVertex(),    // Vertex factory
                numParts, 
                new StandardWorkerOutputMaker(),      // WorkerWriter
                new StandardWorkerGraphMaker(),       // WorkerGraphMaker
                new StandardMasterGraphMaker(),       // MasterGraphMaker
                new StandardMasterOutputMaker()       // Writer 
                );
        job.setProblemAggregator( new IntegerSumAggregator() );
        System.out.println( job );    
        Client.run( job, isEc2Run, numWorkers); //TODO fix this
        System.exit( 0 );
    }
}
