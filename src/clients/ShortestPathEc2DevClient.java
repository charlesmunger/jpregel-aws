package clients;

import JpAws.Ec2ReservationService;
import api.MachineGroup;
import java.util.concurrent.Future;
import system.*;

/**
 *
 * @author charlesmunger
 */
public class ShortestPathEc2DevClient extends Client {
    public static void main(String[] args) throws Exception {
        int numWorkers = Integer.parseInt(args[1]);
        Ec2ReservationService rs = new Ec2ReservationService();
        Future<MachineGroup<ClientToMaster>> masterMachine = rs.reserveMaster("m1.small");
        Future<MachineGroup<Worker>> workers = rs.reserveWorkers("m1.small", numWorkers);
        Future<ClientToMaster> deployMaster = masterMachine.get().deploy(args[1]);
        workers.get().deploy(masterMachine.get().getHostname());
        
        int computeThreadsPerWorker = Runtime.getRuntime().availableProcessors();
        int numParts = numWorkers * computeThreadsPerWorker * 2; 
        
        Job job = new Job(
                "ShortestPath",   // jobName,
                args[0],          // jobDirectoryName,
                new VertexShortestPath(), 
                numParts,
                new MasterGraphMakerG1(),
                new WorkerGraphMakerStandard(),
                new MasterOutputMakerStandard(),
                new WorkerOutputMakerStandard()
                );
        System.out.println( job + "\n        numWorkers:" + numWorkers );
        System.out.println(deployMaster.get().run(job, true));
        masterMachine.get().terminate();
        workers.get().terminate();
    }
}
