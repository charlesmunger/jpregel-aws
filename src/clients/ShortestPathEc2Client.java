package clients;

import JpAws.Ec2ReservationService;
import system.*;

/**
 *
 * @author charlesmunger
 */
public class ShortestPathEc2Client extends Client {
    public static void main(String[] args) throws Exception {
        int numWorkers = Integer.parseInt(args[1]);
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
        System.out.println(Ec2ReservationService.newSmallCluster(numWorkers).run(job, true));
        System.out.println("Currently, termination of premade clusters is not implemented.");
        System.out.println("Please don't forget to terminate via the webui.");
    }
}
