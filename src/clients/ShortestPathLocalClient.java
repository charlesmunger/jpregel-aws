package clients;

import system.*;

/**
 *
 * @author Pete Cappello
 */
public class ShortestPathLocalClient 
{
    /**
     * @param args [0]: Job Directory
     *             [1]: Number of Workers
     */
    public static void main(String[] args) throws Exception 
    {
        int numWorkers = Integer.parseInt(args[1]);
        int computeThreadsPerWorker = Runtime.getRuntime().availableProcessors();
        int numParts = numWorkers * computeThreadsPerWorker * 2;
        Job job = new Job("Shortest Path Problem", // jobName
                  args[0],                         // jobDirectoryName
                  new VertexShortestPath(),        // vertexFactory, 
                  numParts,
                  new MasterGraphMakerG1(),
                  new WorkerGraphMakerStandard(),
                  new MasterOutputMakerStandard(),
                  new WorkerOutputMakerStandard(),
                  new AggregatorSumInteger(),   // problem aggregator
                  new AggregatorSumInteger()    // step    agregator
                );
        System.out.println(job + "\n         numWorkers: " + numWorkers );
        ClientToMaster master = LocalReservationService.newLocalCluster(numWorkers);
        System.out.println(master.run(job));
        System.exit(0);
    }
}
