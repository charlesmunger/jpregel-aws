package clients;

import JpAws.Ec2ReservationService;
import api.MachineGroup;
import system.*;

/**
 *
 * @author charlesmunger
 */
public class ShortestPathEc2DevClient extends Client {
    public static void main(String[] args) throws Exception {
        int numWorkers = Integer.parseInt(args[1]);
        Ec2ReservationService rs = new Ec2ReservationService();
        MachineGroup master = rs.reserveMaster("m1.small");
        MachineGroup workers = rs.reserveWorkers("m1.small", numWorkers);
        ClientToMaster deploy = (ClientToMaster) master.deploy(args[1]);
        workers.deploy(master.getHostname());
        
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
        deploy.run(job, true);
        master.terminate();
        workers.terminate();
    }
}
