package clients;

import JpAws.Machine;
import static java.lang.System.out;

import java.rmi.RemoteException;
import vertex.TestMutabilityVertex;
import masterGraphMakers.StandardMasterGraphMaker;
import masterOutputMakers.StandardMasterOutputMaker;
import system.Client;
import system.Combiner;
import system.GraphMaker;
import system.Job;
import system.MasterGraphMaker;
import system.Vertex;
import system.WorkerWriter;
import system.Writer;
import system.combiners.DoubleMinCombiner;
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
        boolean combiningMessages     = Boolean.parseBoolean( args[3] );
        int     numParts = numWorkers * 2 * 2; // numWorkers * ComputeThrads/Worker * Parts/ComputeThread
        Combiner combiner = null;
        if ( combiningMessages )
        {
            combiner = new DoubleMinCombiner();
        }
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
                + "\n combining messages: " + combiningMessages
                );
        
        Job job = new Job( jobName,
                jobDirectoryName, 
                vertexFactory, numParts, workerIsMultithreaded, combiner, 
                workerWriter, workerGraphMaker, reader, writer );
        Job[] jobs = { job };
        try
        {
            boolean   isEc2Master = false;
            Client.run( jobs, isEc2Master, numWorkers,Machine.AMIID,Machine.AMIID );//TODO fix me
        } 
        catch ( Exception exception )
        {
            exception.printStackTrace();
            System.exit(1);
        }
        System.exit( 0 );
    }
}
