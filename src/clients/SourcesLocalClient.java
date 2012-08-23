package clients;

import system.ClientToMaster;
import system.Job;
import system.LocalReservationService;
import system.MasterGraphMakerStandard;
import system.MasterOutputMakerStandard;
import system.VertexSources;
import system.WorkerGraphMakerStandard;
import system.WorkerOutputMakerStandard;

/**
 *
 * @author Pete Cappello
 */
public class SourcesLocalClient
{
    /**
     * @param args [0]: Job directory name
     */
    public static void main( String[] args ) throws Exception
    {
        int numWorkers = 1; 
        int computeThreadsPerWorker = Runtime.getRuntime().availableProcessors();
        int partsPerComputeThread = 2;
        int numParts = numWorkers * computeThreadsPerWorker * partsPerComputeThread;        
        Job job = new Job(
                "Identify source nodes",            // jobName
                args[0],                            // jobDirectoryName
                new VertexSources(), // vertexFactory
                numParts,
                new MasterGraphMakerStandard(),  
                new WorkerGraphMakerStandard(),   
                new MasterOutputMakerStandard(),
                new WorkerOutputMakerStandard()                 
                );
        System.out.println( job + "\n    numWorkers: " + numWorkers );
        ClientToMaster master = LocalReservationService.newLocalCluster(numWorkers);
        System.out.println(master.run(job));
        System.exit( 0 );
    }
}
