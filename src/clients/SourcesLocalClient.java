package clients;

import system.Client;
import system.Job;
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
        boolean isEc2Master = false;
        Client.run( job, isEc2Master, numWorkers);
        System.exit( 0 );
    }
}
