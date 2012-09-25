package clients;

import JpLAN.LANReservationService;
import system.*;

/**
 *
 * @author charlesmunger
 */
public class LanDevClient {
    public static void main(String[] args) throws Exception {
        int numWorkers = Integer.parseInt(args[1]);
        Job job = new Job("Shortest Path Problem", // jobName
                  args[0],                         // jobDirectoryName
                  new VertexShortestPath(),        // vertexFactory, 
                  new MasterGraphMakerG1(),
                  new WorkerGraphMakerStandard(),
                  new MasterOutputMakerStandard(),
                  new WorkerOutputMakerStandard(),
                  new AggregatorSumInteger(),   // problem aggregator
                  new AggregatorSumInteger()    // step    agregator
                );
        System.out.println(job + "\n         numWorkers: " + numWorkers );
        System.out.println(LANReservationService.newLocalCluster(numWorkers).run(job));
        System.exit(0);
    }
}
