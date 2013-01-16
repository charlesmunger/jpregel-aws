package clients;

import edu.ucsb.jpregel.system.WorkerOutputMakerStandard;
import edu.ucsb.jpregel.system.VertexShortestPathEuclidean;
import edu.ucsb.jpregel.system.MasterOutputMakerStandard;
import edu.ucsb.jpregel.system.AggregatorSumInteger;
import edu.ucsb.jpregel.system.ClientToMaster;
import edu.ucsb.jpregel.system.Job;
import edu.ucsb.jpregel.system.LocalReservationService;
import edu.ucsb.jpregel.system.MasterGraphMakerGrid;
import edu.ucsb.jpregel.system.WorkerGraphMakerGrid;

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

        Job job = new Job( 
                "Euclidean 2D Grid Shortest Path",
                args[0],     // job directory name
                new VertexShortestPathEuclidean(),
                new MasterGraphMakerGrid(),
                new WorkerGraphMakerGrid(),
                new MasterOutputMakerStandard(),
                new WorkerOutputMakerStandard(),
                new AggregatorSumInteger(),   // problem aggregator
                null                          // step    agregator
                );
        System.out.println( job + "\n      numWorkers: " + numWorkers );
        
        System.out.println("Euclidean2DGridShortestPathClient.main: about to invoke Client.run");
        ClientToMaster master = LocalReservationService.newCluster(numWorkers);
        System.out.println(master.run(job));        System.exit( 0 );
    }
}
