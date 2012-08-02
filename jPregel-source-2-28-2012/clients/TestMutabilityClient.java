package clients;

import static java.lang.System.out;

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
    public static void main( String[] args ) throws Exception
    {
        String  jobName               = "Test Graph Mutability Features";
        String  jobDirectoryName      = args[0];
        int     computeThreadsPerWorker = Runtime.getRuntime().availableProcessors();
        int     numWorkers            = Integer.parseInt( args[1] );
        int     numParts = numWorkers * computeThreadsPerWorker * 2; // numWorkers * ComputeThrads/Worker * Parts/ComputeThread
        WorkerWriter workerWriter = new StandardWorkerOutputMaker();
        GraphMaker workerGraphMaker = new StandardWorkerGraphMaker();
        MasterGraphMaker reader = new StandardMasterGraphMaker();
        Writer writer = new StandardMasterOutputMaker();
        Vertex vertexFactory = new TestMutabilityVertex();
                
        Job job = new Job( jobName,
                  jobDirectoryName, 
                  vertexFactory, numParts,  
                  workerWriter, workerGraphMaker, reader, writer );
        System.out.println( "TestMutabilityClient: numWorkers: " + numWorkers + job );
        boolean isEc2Master = false;
        Client.run( job, isEc2Master, numWorkers);
        System.exit( 0 );
    }
}
