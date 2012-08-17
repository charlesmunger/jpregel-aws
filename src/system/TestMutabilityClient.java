package system;


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
        int computeThreadsPerWorker = Runtime.getRuntime().availableProcessors();
        int numWorkers = Integer.parseInt( args[1] );
        int numParts = numWorkers * computeThreadsPerWorker * 2; 
        Job job = new Job( 
                "Test Graph Mutability Features", 
                args[0],      // jobDirectoryName, 
                new VertexTestMutability(), 
                numParts,
                new MasterGraphMakerStandard(),
                new WorkerGraphMakerStandard(),
                new MasterOutputMakerStandard(),
                new WorkerOutputMakerStandard()
                );
        System.out.println( job + "\n        numWorkers: " + numWorkers );
        boolean isEc2Master = false;
        Client.run( job, isEc2Master, numWorkers);
        System.exit( 0 );
    }
}
