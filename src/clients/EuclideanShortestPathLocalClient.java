package clients;

import system.*;

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
        int computeThreadsPerWorker = Runtime.getRuntime().availableProcessors();
        int partsPerComputeThread = 2;
        int numParts = numWorkers * computeThreadsPerWorker * partsPerComputeThread;

        Job job = new Job(
                "Euclidean Shortest Path", // Job name
                args[0], // Job directory name
                new VertexShortestPathEuclidean(), // Vertex factory
                numParts,
                new MasterGraphMakerStandard(),
                new WorkerGraphMakerStandard(),
                new MasterOutputMakerStandard(),
                new WorkerOutputMakerStandard(),
                new AggregatorSumInteger(), // problem aggregator
                null // step    agregator
                );
        System.out.println(job + "\n    numWorkers: " + numWorkers);
        ClientToMaster master = LocalReservationService.newLocalCluster(numWorkers);
        System.out.println(master.run(job));
        System.exit(0);
    }
}
