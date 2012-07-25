package clients;

import java.rmi.RemoteException;
import masterGraphMakers.G1MasterGraphMaker;
import masterOutputMakers.StandardMasterOutputMaker;
import system.*;
import vertex.ShortestPathVertex;
import workerGraphMakers.StandardWorkerGraphMaker;
import workerOutputMakers.StandardWorkerOutputMaker;

/**
 *
 * @author Pete Cappello
 */
public class ShortestPathEc2Client extends Client 
{
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws RemoteException 
    {
        String jobName = "ShortestPath";
        String jobDirectoryName = args[0];
        int numWorkers = Integer.parseInt(args[1]);
        int numParts = numWorkers * 2 * 2; // numParts = numWorkers * ComputeThreads/Worker * Parts/ComputeThread;
        boolean workerIsMultithreaded = Boolean.parseBoolean(args[3]);
        boolean isEc2Master = true;
        Vertex vertexFactory = new ShortestPathVertex();
        WorkerWriter workerWriter = new StandardWorkerOutputMaker();
        GraphMaker workerGraphMaker = new StandardWorkerGraphMaker();
        MasterGraphMaker reader = new G1MasterGraphMaker();
        Writer writer = new StandardMasterOutputMaker();
        Job job = new Job(jobName,
                jobDirectoryName,
                vertexFactory, numParts, workerIsMultithreaded, 
                workerWriter, workerGraphMaker, reader, writer);
        try
        {
            Client.run(job, isEc2Master, numWorkers);
        }
        catch ( Exception exception )
        {
            exception.printStackTrace();
            System.exit( 1 );
        }
                
        System.exit(0);
    }
}
