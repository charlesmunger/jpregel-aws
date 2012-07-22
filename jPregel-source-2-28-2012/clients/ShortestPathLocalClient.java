package clients;

import java.rmi.RemoteException;
import masterGraphMakers.G1MasterGraphMaker;
import masterOutputMakers.StandardMasterOutputMaker;
import system.*;
import system.aggregators.IntegerSumAggregator;
import system.combiners.IntegerMinCombiner;
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
     *             [3]: true if and only if messages are to be combined
     */
    public static void main(String[] args) throws RemoteException 
    {
        int numWorkers = Integer.parseInt(args[1]);
        boolean combiningMessages = Boolean.parseBoolean(args[3]);
        int numParts = numWorkers * 2 * 2; // numWorkers * ComputeThreads/Worker * Parts/ComputeThread
        Combiner combiner = null;
        if (combiningMessages) 
        {
            combiner = new IntegerMinCombiner();
        }

        Job job = new Job("Shortest Path Problem", // jobName
                  args[0],                         // jobDirectoryName
                  new ShortestPathVertex(),        // vertexFactory, 
                  numParts, 
                  Boolean.parseBoolean(args[2]),   // workerIsMultithreaded, 
                  combiner, 
                  new StandardWorkerOutputMaker(), // workerWriter,
                  new StandardWorkerGraphMaker(),  // workerGraphMaker, 
                  new G1MasterGraphMaker(),        // MasterGraphMaker, 
                  new StandardMasterOutputMaker()  // Writer
                );
        job.setProblemAggregator(new IntegerSumAggregator());
        job.setStepAggregator(new IntegerSumAggregator());
        System.out.println("ShortestPathLocalClient.main: numWorkers: " + numWorkers + "\n " + job );
        try 
        {
            boolean isEc2Master = false;
            Client.run(job, isEc2Master, numWorkers); //TODO fix this
        } catch (Exception exception) 
        {
            exception.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
}
