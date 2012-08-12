package clients;

import api.MasterOutputMaker;
import api.MasterGraphMaker;
import api.WorkerGraphMaker;
import api.WorkerOutputMaker;
import system.MasterGraphMakerG1;
import system.MasterOutputMakerStandard;
import system.*;
import system.VertexShortestPath;
import system.WorkerGraphMakerStandard;
import system.WorkerOutputMakerStandard;

/**
 *
 * @author Pete Cappello
 */
public class ShortestPathEc2Client extends Client 
{
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception 
    {
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
        boolean isEc2Master = true;
        Client.run(job, isEc2Master, numWorkers);     
        System.exit(0);
    }
}
