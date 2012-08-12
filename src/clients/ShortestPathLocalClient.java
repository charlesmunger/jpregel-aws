package clients;

import system.MasterGraphMakerG1;
import system.MasterOutputMakerStandard;
import system.Client;
import system.Job;
import system.AggregatorSumInteger;
import system.VertexShortestPath;
import system.WorkerGraphMakerStandard;
import system.WorkerOutputMakerStandard;

/**
 *
 * @author Pete Cappello
 */
public class ShortestPathLocalClient 
{
    /**
     * @param args [0]: Job Directory
     *             [1]: Number of Workers
     */
    public static void main(String[] args) throws Exception 
    {
        int numWorkers = Integer.parseInt(args[1]);
        int computeThreadsPerWorker = Runtime.getRuntime().availableProcessors();
        int numParts = numWorkers * computeThreadsPerWorker * 2;
        Job job = new Job("Shortest Path Problem", // jobName
                  args[0],                         // jobDirectoryName
                  new VertexShortestPath(),        // vertexFactory, 
                  numParts, 
                  new WorkerOutputMakerStandard(), // workerWriter,
                  new WorkerGraphMakerStandard(),  // workerGraphMaker, 
                  new MasterGraphMakerG1(),        // MasterGraphMaker, 
                  new MasterOutputMakerStandard()  // Writer
                );
        job.setProblemAggregator(new AggregatorSumInteger());
        job.setStepAggregator(new AggregatorSumInteger());
        System.out.println("ShortestPathLocalClient.main: numWorkers: " + numWorkers + "\n " + job );
        boolean isEc2Master = false;
        Client.run(job, isEc2Master, numWorkers);
        System.exit(0);
    }
}
