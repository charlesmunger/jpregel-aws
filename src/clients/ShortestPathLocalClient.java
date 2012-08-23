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
     */
    public static void main(String[] args) throws Exception 
    {
        int numWorkers = Integer.parseInt( args[1] );
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
        Client.run(job, isEc2Master, numWorkers);
        System.exit(0);
    }
}
