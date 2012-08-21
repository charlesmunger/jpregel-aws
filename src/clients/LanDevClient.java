package clients;

import JpLAN.LANReservationService;
import api.MachineGroup;
import api.ReservationService;
import system.*;

/**
 *
 * @author charlesmunger
 */
public class LanDevClient {
    public static void main(String[] args) throws Exception {
        ReservationService rs = new LANReservationService();
        MachineGroup master = rs.reserveMaster(null);
        MachineGroup workers = rs.reserveWorkers(null, Integer.parseInt(args[1]));
        ClientToMaster deploy = (ClientToMaster) master.deploy((String) null);
        workers.deploy(master.getHostname());
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
        boolean isEc2Master = false;
        deploy.run(job, isEc2Master);
        System.exit(0);
    }
}
