package clients;

import java.rmi.RemoteException;
import masterGraphMakers.G1MasterGraphMaker;
import masterOutputMakers.StandardMasterOutputMaker;
import system.*;
import system.combiners.IntegerMinCombiner;
import vertex.ShortestPathVertex;
import workerGraphMakers.StandardWorkerGraphMaker;
import workerOutputMakers.StandardWorkerOutputMaker;

/**
 *
 * @author Pete Cappello
 */
public class ShortestPathEc2Client extends Client {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws RemoteException {
        String jobName = "ShortestPath";
        String jobDirectoryName = args[0];
        int numParts = Integer.parseInt(args[1]);
        int numWorkers = Integer.parseInt(args[2]);
        boolean workerIsMultithreaded = Boolean.parseBoolean(args[3]);
        boolean isEc2Master = true;
        boolean combiningMessages = Boolean.parseBoolean(args[4]);;   
        System.out.println("Testing 123");
        Combiner combiner = null;
        if (combiningMessages) {
            combiner = new IntegerMinCombiner();
        }
        Vertex vertexFactory = new ShortestPathVertex();
        WorkerWriter workerWriter = new StandardWorkerOutputMaker();
        GraphMaker workerGraphMaker = new StandardWorkerGraphMaker();
        MasterGraphMaker reader = new G1MasterGraphMaker();
        Writer writer = new StandardMasterOutputMaker();
        Job job = new Job(jobName,
                jobDirectoryName,
                vertexFactory, numParts, workerIsMultithreaded, combiner,
                workerWriter, workerGraphMaker, reader, writer);
        Job[] jobs = {job};
        Client.run(jobs, isEc2Master, numWorkers);        
        System.exit(0);
    }
}
