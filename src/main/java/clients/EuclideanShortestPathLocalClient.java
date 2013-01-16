package clients;

import edu.ucsb.jpregel.system.WorkerOutputMakerStandard;
import edu.ucsb.jpregel.system.VertexShortestPathEuclidean;
import edu.ucsb.jpregel.system.MasterOutputMakerStandard;
import edu.ucsb.jpregel.system.AggregatorSumInteger;
import edu.ucsb.jpregel.system.ClientToMaster;
import edu.ucsb.jpregel.system.MasterGraphMakerStandard;
import edu.ucsb.jpregel.system.WorkerGraphMakerStandard;
import edu.ucsb.jpregel.system.Job;
import edu.ucsb.jpregel.system.LocalReservationService;

/**
 *
 * @author Pete Cappello
 */
public class EuclideanShortestPathLocalClient
{

    /**
     * @param args [0]: Job Directory [1]: Number of Workers [2]: true if and
     * only if worker is to be multi-threaded
     */
    public static void main(String[] args) throws Exception
    {
        int numWorkers = Integer.parseInt(args[1]);

        Job job = new Job(
                "Euclidean Shortest Path", // Job name
                args[0], // Job directory name
                new VertexShortestPathEuclidean(), // Vertex factory
                new MasterGraphMakerStandard(),
                new WorkerGraphMakerStandard(),
                new MasterOutputMakerStandard(),
                new WorkerOutputMakerStandard(),
                new AggregatorSumInteger(), // problem aggregator
                null // step    agregator
                );
        System.out.println(job + "\n    numWorkers: " + numWorkers);
        ClientToMaster master = LocalReservationService.newCluster(numWorkers);
        System.out.println(master.run(job));
        System.exit(0);
    }
}
