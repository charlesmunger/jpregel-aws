package clients;

import masterGraphMakers.G1MasterGraphMaker;
import masterOutputMakers.StandardMasterOutputMaker;
import system.*;
import system.aggregators.IntegerSumAggregator;
import vertex.ShortestPathVertex;
import workerGraphMakers.StandardWorkerGraphMaker;
import workerOutputMakers.StandardWorkerOutputMaker;

/**
 *
 * @author Pete Cappello
 */
public class ShortestPathLocalClient 
{
    /**
     * @param args [0]: Job Directory
     *             [1]: Number of Workers
     *             [2]: true if and only if worker is to be multi-threaded
     */
    public static void main(String[] args) throws Exception 
    {
        int numWorkers = Integer.parseInt(args[1]);
        int     computeThreadsPerWorker = Runtime.getRuntime().availableProcessors();
        int numParts = numWorkers * computeThreadsPerWorker * 2; // numWorkers * ComputeThreads/Worker * Parts/ComputeThread
        Job job = new Job("Shortest Path Problem", // jobName
                  args[0],                         // jobDirectoryName
                  new ShortestPathVertex(),        // vertexFactory, 
                  numParts, 
                  new StandardWorkerOutputMaker(), // workerWriter,
                  new StandardWorkerGraphMaker(),  // workerGraphMaker, 
                  new G1MasterGraphMaker(),        // MasterGraphMaker, 
                  new StandardMasterOutputMaker()  // Writer
                );
        job.setProblemAggregator(new IntegerSumAggregator());
        job.setStepAggregator(new IntegerSumAggregator());
        System.out.println("ShortestPathLocalClient.main: numWorkers: " + numWorkers + "\n " + job );
        boolean isEc2Master = false;
        Client.run(job, isEc2Master, numWorkers);
        System.exit(0);
    }
}
