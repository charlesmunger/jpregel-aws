package clients;

import JpAws.Machine;
import static java.lang.System.out;
import java.rmi.RemoteException;
import masterGraphMakers.GridMasterGraphMaker;
import masterOutputMakers.StandardMasterOutputMaker;
import system.*;
import system.aggregators.IntegerSumAggregator;
import system.combiners.FloatMinCombiner;
import vertex.EuclideanShortestPathVertex;
import workerGraphMakers.GridWorkerGraphMaker;
import workerOutputMakers.StandardWorkerOutputMaker;

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
        String  jobName               = "Euclidean 2D Grid Shortest Path";
        String  jobDirectoryName      = args[0];
        int     numWorkers            = 1;
        boolean workerIsMultithreaded = false;
        int     numParts = numWorkers * (( workerIsMultithreaded) ? 2 : 1 ) * 1; // numWorkers * ComputeThrads/Worker * Parts/ComputeThread
        Vertex vertexFactory        = new EuclideanShortestPathVertex();
        WorkerWriter workerWriter   = new StandardWorkerOutputMaker();
        GraphMaker workerGraphMaker = new GridWorkerGraphMaker();
        MasterGraphMaker reader     = new GridMasterGraphMaker();
        Writer writer               = new StandardMasterOutputMaker();
        
        out.println("Euclidean2DGridShortestPathClient.main: "
                + "\n jobDirectoryName: " + jobDirectoryName
                + "\n numParts: " + numParts
                + "\n numWorkers: " + numWorkers
                + "\n workerIsMultithreaded: " + workerIsMultithreaded
                );
        
        Job job = new Job( jobName, jobDirectoryName, vertexFactory, numParts, 
                           workerIsMultithreaded,  
                workerWriter, 
                           workerGraphMaker, reader, writer );
        job.setProblemAggregator( new IntegerSumAggregator() );
        boolean   isEc2Master = false;
        System.out.println("Euclidean2DGridShortestPathClient.main: about to invoke Client.run");
        Client.run( job, isEc2Master, numWorkers); 
        System.exit( 0 );
    }
}
