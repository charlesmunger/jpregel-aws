package clients;

import system.*;

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
        
        System.out.println("Euclidean2DGridShortestPathClient.main: about to invoke Client.run");
        ClientToMaster master = LocalReservationService.newLocalCluster(numWorkers);
        System.out.println(master.run(job));        System.exit( 0 );
    }
}
