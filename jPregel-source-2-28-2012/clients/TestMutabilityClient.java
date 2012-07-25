package clients;

import static java.lang.System.out;

import java.rmi.RemoteException;
import vertex.TestMutabilityVertex;
import masterGraphMakers.StandardMasterGraphMaker;
import masterOutputMakers.StandardMasterOutputMaker;
import system.Client;
import system.GraphMaker;
import system.Job;
import system.MasterGraphMaker;
import system.Vertex;
import system.WorkerWriter;
import system.Writer;
import workerGraphMakers.StandardWorkerGraphMaker;
import workerOutputMakers.StandardWorkerOutputMaker;

/**
 *
 * @author Pete Cappello
 */
public class TestMutabilityClient 
{
    /**
     * @param args the command line arguments
     */
    public static void main( String[] args ) throws RemoteException
    {
        String  jobName               = "Test Graph Mutability Features";
        String  jobDirectoryName      = args[0];
        int     numWorkers            = Integer.parseInt(     args[1] );
        boolean workerIsMultithreaded = Boolean.parseBoolean( args[2] );
        int     numParts = numWorkers * 2 * 2; // numWorkers * ComputeThrads/Worker * Parts/ComputeThread
        WorkerWriter workerWriter = new StandardWorkerOutputMaker();
        GraphMaker workerGraphMaker = new StandardWorkerGraphMaker();
        MasterGraphMaker reader = new StandardMasterGraphMaker();
        Writer writer = new StandardMasterOutputMaker();
        Vertex vertexFactory = new TestMutabilityVertex();
        
        out.println("ShortestPathDevelopmentClient.main: "
                + "\n jobDirectoryName: " + jobDirectoryName
                + "\n numParts: " + numParts
                + "\n numWorkers: " + numWorkers
                + "\n workerIsMultithreaded: " + workerIsMultithreaded
                );
        
        Job job = new Job( jobName,
                  jobDirectoryName, 
                  vertexFactory, numParts, workerIsMultithreaded,  
                  workerWriter, workerGraphMaker, reader, writer );
        try
        {
            boolean isEc2Master = false;
            Client.run( job, isEc2Master, numWorkers);
        } 
        catch ( Exception exception )
        {
            exception.printStackTrace();
            System.exit(1);
        }
        System.exit( 0 );
    }
}
