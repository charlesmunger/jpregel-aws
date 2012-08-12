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
        String jobName = "ShortestPath";
        String jobDirectoryName = args[0];
        int numWorkers = Integer.parseInt(args[1]);
        int     computeThreadsPerWorker = Runtime.getRuntime().availableProcessors();
        int numParts = numWorkers * computeThreadsPerWorker * 2; // numParts = numWorkers * ComputeThreads/Worker * Parts/ComputeThread;
        boolean workerIsMultithreaded = Boolean.parseBoolean(args[3]);
        boolean isEc2Master = true;
        VertexImpl vertexFactory = new VertexShortestPath();
        WorkerOutputMaker workerWriter = new WorkerOutputMakerStandard();
        WorkerGraphMaker workerGraphMaker = new WorkerGraphMakerStandard();
        MasterGraphMaker reader = new MasterGraphMakerG1();
        MasterOutputMaker writer = new MasterOutputMakerStandard();
        Job job = new Job(jobName,
                jobDirectoryName,
                vertexFactory, numParts,  
                workerWriter, workerGraphMaker, reader, writer);
        System.out.println( job );
        Client.run(job, isEc2Master, numWorkers);     
        System.exit(0);
    }
}
