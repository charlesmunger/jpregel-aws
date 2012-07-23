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
     *             [3]: true if and only if messages are to be combined
     */
    public static void main( String[] args ) throws RemoteException
    {
        int     numWorkers              = Integer.parseInt(     args[1] );
        boolean isMultithreaded         = Boolean.parseBoolean( args[2] );
        int     computeThreadsPerWorker = isMultithreaded ? Runtime.getRuntime().availableProcessors() : 1;
        int     partsPerComputeThread   = 2;
        int     numParts                = numWorkers * computeThreadsPerWorker * partsPerComputeThread;
        boolean combiningMessages       = Boolean.parseBoolean( args[3] );
        boolean isEc2Run                = Boolean.parseBoolean( args[4] );
        Combiner combiner = null;
        if ( combiningMessages )
        {
            combiner = new FloatMinCombiner();
        }
        
        System.out.println("EuclideanShortestPathLocalClient: numWorkers: " + numWorkers + " isAWS run: " + isEc2Run);
        
        Job job = new Job( 
                "Euclidean Shortest Path",            // Job name
                args[0],                              // Job directory name
                new EuclideanShortestPathVertex(),    // Vertex factory
                numParts, 
                Boolean.parseBoolean( args[2] ) ,     // Worker Is Multithreaded
                combiner, 
                new StandardWorkerOutputMaker(),      // WorkerWriter
                new StandardWorkerGraphMaker(),       // WorkerGraphMaker
                new StandardMasterGraphMaker(),       // MasterGraphMaker
                new StandardMasterOutputMaker()       // Writer 
                );
        job.setProblemAggregator( new IntegerSumAggregator() );
        System.out.println( job );
        try
        {
            Client.run( job, isEc2Run, numWorkers); //TODO fix this
        } 
        catch ( Exception exception )
        {
            exception.printStackTrace();
            System.exit(1);
        }
        System.exit( 0 );
    }
}
