package clients;


import system.VertexTestMutability;
import system.MasterGraphMakerStandard;
import system.MasterOutputMakerStandard;
import system.Client;
import system.Job;
import api.MasterGraphMaker;
import system.VertexImpl;
import api.MasterOutputMaker;
import api.WorkerGraphMaker;
import api.WorkerOutputMaker;
import system.WorkerGraphMakerStandard;
import system.WorkerOutputMakerStandard;

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
        WorkerOutputMaker workerWriter = new WorkerOutputMakerStandard();
        WorkerGraphMaker workerGraphMaker = new WorkerGraphMakerStandard();
        MasterGraphMaker reader = new MasterGraphMakerStandard();
        MasterOutputMaker writer = new MasterOutputMakerStandard();
        VertexImpl vertexFactory = new VertexTestMutability();
                
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
