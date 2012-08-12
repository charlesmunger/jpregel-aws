package clients;

import api.MasterOutputMaker;
import api.MasterGraphMaker;
import api.WorkerGraphMaker;
import api.WorkerOutputMaker;
import static java.lang.System.out;
import system.MasterGraphMakerGrid;
import system.MasterOutputMakerStandard;
import system.*;
import system.AggregatorSumInteger;
import system.VertexShortestPathEuclidean;
import system.WorkerGraphMakerGrid;
import system.WorkerOutputMakerStandard;

/**
 *
 * @author Pete Cappello
 */
public class Euclidean2DGridShortestPathClient 
{
    /**
     * @param args the command line arguments
     */
    public static void main( String[] args ) throws Exception
    {
        String  jobName             = "Euclidean 2D Grid Shortest Path";
        String  jobDirectoryName    = args[0];
        int     numWorkers          = 1;
        int computeThreadsPerWorker = Runtime.getRuntime().availableProcessors();
        int     numParts = numWorkers * computeThreadsPerWorker * 2; // numWorkers * ComputeThrads/Worker * Parts/ComputeThread
        VertexImpl vertexFactory        = new VertexShortestPathEuclidean();
        WorkerOutputMaker workerWriter   = new WorkerOutputMakerStandard();
        WorkerGraphMaker workerGraphMaker = new WorkerGraphMakerGrid();
        MasterGraphMaker reader     = new MasterGraphMakerGrid();
        MasterOutputMaker writer               = new MasterOutputMakerStandard();
        
        out.println("Euclidean2DGridShortestPathClient.main: "
                + "\n jobDirectoryName: " + jobDirectoryName
                + "\n numParts: " + numParts
                + "\n numWorkers: " + numWorkers
                );
        
        Job job = new Job( jobName, jobDirectoryName, vertexFactory, numParts, 
                workerWriter, workerGraphMaker, reader, writer );
        job.setProblemAggregator( new AggregatorSumInteger() );
        boolean   isEc2Master = false;
        System.out.println("Euclidean2DGridShortestPathClient.main: about to invoke Client.run");
        Client.run( job, isEc2Master, numWorkers); 
        System.exit( 0 );
    }
}
