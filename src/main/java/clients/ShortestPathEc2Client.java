package clients;

import edu.ucsb.jpregel.system.MasterGraphMakerG1;
import edu.ucsb.jpregel.system.WorkerOutputMakerStandard;
import edu.ucsb.jpregel.system.MasterOutputMakerStandard;
import edu.ucsb.jpregel.system.WorkerGraphMakerStandard;
import edu.ucsb.jpregel.system.Job;
import edu.ucsb.jpregel.system.VertexShortestPath;
import edu.ucsb.jpregel.aws.Ec2ReservationService;

/**
 *
 * @author charlesmunger
 */
public class ShortestPathEc2Client  {
    public static void main(String[] args) throws Exception {
        int numWorkers = Integer.parseInt(args[1]);
        
        Job job = new Job(
                "ShortestPath",   // jobName,
                args[0],          // jobDirectoryName,
                new VertexShortestPath(), 
                new MasterGraphMakerG1(),
                new WorkerGraphMakerStandard(),
                new MasterOutputMakerStandard(),
                new WorkerOutputMakerStandard()
                );
        System.out.println( job + "\n        numWorkers:" + numWorkers );
        System.out.println(Ec2ReservationService.newMassiveCluster(numWorkers).run(job));
        System.out.println("Currently, termination of premade clusters is not implemented.");
        System.out.println("Please don't forget to terminate via the webui.");
    }
}
