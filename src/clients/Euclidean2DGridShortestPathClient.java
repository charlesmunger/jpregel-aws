package clients;

import api.MasterOutputMaker;
import api.MasterGraphMaker;
import api.WorkerGraphMaker;
import api.WorkerOutputMaker;
import static java.lang.System.out;
import system.MasterGraphMakerGrid;
import system.MasterOutputMakerStandard;
import system.*;
import system.AggregatorSumInteger;
import system.VertexShortestPathEuclidean;
import system.WorkerGraphMakerGrid;
import system.WorkerOutputMakerStandard;

/**
 *
 * @author Pete Cappello
 */
public class Euclidean2DGridShortestPathClient 
{
    /**
     * @param args the command line arguments
     */
    public static void main( String[] args ) throws Exception
    {
        int numWorkers = 1;
        int computeThreadsPerWorker = Runtime.getRuntime().availableProcessors();
        int numParts = numWorkers * computeThreadsPerWorker * 2; // numWorkers * ComputeThrads/Worker * Parts/ComputeThread
        
        Job job = new Job( 
                "Euclidean 2D Grid Shortest Path",
                args[0],     // job directory name
                new VertexShortestPathEuclidean(),
                numParts,
                new MasterGraphMakerGrid(),
                new WorkerGraphMakerGrid(),
                new MasterOutputMakerStandard(),
                new WorkerOutputMakerStandard(),
                new AggregatorSumInteger(),   // problem aggregator
                null                          // step    agregator
                );
        System.out.println( job + "\n      numWorkers: " + numWorkers );
        
        boolean   isEc2Master = false;
        System.out.println("Euclidean2DGridShortestPathClient.main: about to invoke Client.run");
        Client.run( job, isEc2Master, numWorkers); 
        System.exit( 0 );
    }
}
