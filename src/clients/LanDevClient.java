package clients;

import JpLAN.LANReservationService;
import system.*;
import vertices.VertexShortestPathBinaryTree;

/**
 *
 * @author charlesmunger
 */
public class LanDevClient {
    public static void main(String[] args) throws Exception {
        int numWorkers = Integer.parseInt(args[1]);
        Job job = new Job("Shortest Path Problem", // jobName
                  args[0],                         // jobDirectoryName
                  new VertexShortestPathBinaryTree(),        // vertexFactory, 
                  new MasterGraphMakerBinaryTree(),
                  new WorkerGraphMakerBinaryTree(),
                  new MasterOutputMakerStandard(),
                  new WorkerOutputMakerStandard()
                );
        System.out.println(job + "\n         numWorkers: " + numWorkers );
        System.out.println(LANReservationService.newLocalCluster(numWorkers).run(job));
        System.exit(0);
    }
}
