package clients;

import JpAws.Machine;
import static java.lang.System.out;
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
public class ShortestPathDevelopmentClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws RemoteException {
        String jobName = "Shortest Path Problem";
        String jobDirectoryName = args[0];
        int numWorkers = Integer.parseInt(args[1]);
        boolean workerIsMultithreaded = Boolean.parseBoolean(args[2]);
        boolean combiningMessages = Boolean.parseBoolean(args[3]);
        int numParts = numWorkers * 2 * 2; // numWorkers * ComputeThreads/Worker * Parts/ComputeThread
        Combiner combiner = null;
        if (combiningMessages) {
            combiner = new IntegerMinCombiner();
        }
        Vertex vertexFactory = new ShortestPathVertex();
        WorkerWriter workerWriter = new StandardWorkerOutputMaker();
        GraphMaker workerGraphMaker = new StandardWorkerGraphMaker();
        MasterGraphMaker reader = new G1MasterGraphMaker();
        Writer writer = new StandardMasterOutputMaker(jobDirectoryName);

        out.println("ShortestPathDevelopmentClient.main: "
                + "\n jobDirectoryName: " + jobDirectoryName
                + "\n numParts: " + numParts
                + "\n numWorkers: " + numWorkers
                + "\n workerIsMultithreaded: " + workerIsMultithreaded
                + "\n combining messages: " + combiningMessages);

        Job job = new Job(jobName,
                jobDirectoryName,
                vertexFactory, numParts, workerIsMultithreaded, combiner, workerWriter,
                workerGraphMaker, reader, writer);
        job.setProblemAggregator(new IntegerSumAggregator());
        job.setStepAggregator(new IntegerSumAggregator());
        Job[] jobs = {job};
        try {
            boolean isEc2Master = false;
            Client.run(jobs, isEc2Master, numWorkers);//TODO fix this
        } catch (Exception exception) {
            exception.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
}
