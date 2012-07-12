package clients;

import static java.lang.System.out;

import java.rmi.RemoteException;
import vertex.EuclideanShortestPathVertex;
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
import system.aggregators.IntegerSumAggregator;
import system.combiners.FloatMinCombiner;
import workerGraphMakers.StandardWorkerGraphMaker;
import workerOutputMakers.StandardWorkerOutputMaker;

/**
 *
 * @author Pete Cappello
 */
public class EuclideanShortestPathDevelopmentClient 
{
    /**
     * @param args the command line arguments
     */
    public static void main( String[] args ) throws RemoteException
    {
        String  jobName               = "Euclidean Shortest Path";
        String  jobDirectoryName      = args[0];
        int     numWorkers            = Integer.parseInt(     args[1] );
        boolean workerIsMultithreaded = Boolean.parseBoolean( args[2] );
        boolean combiningMessages     = Boolean.parseBoolean( args[3] );
        boolean isEc2Run              = Boolean.parseBoolean( args[4] );
        int     numParts = numWorkers * 2 * 2; // numWorkers * ComputeThrads/Worker * Parts/ComputeThread
        Combiner combiner = null;
        if ( combiningMessages )
        {
            combiner = new FloatMinCombiner();
        }
        Vertex vertexFactory = new EuclideanShortestPathVertex();
        WorkerWriter workerWriter = new StandardWorkerOutputMaker();
        GraphMaker workerGraphMaker = new StandardWorkerGraphMaker();
        MasterGraphMaker reader = new StandardMasterGraphMaker();
        Writer writer = new StandardMasterOutputMaker();
        
        out.println(jobName
                + "\n jobDirectoryName: " + jobDirectoryName
                + "\n numParts: " + numParts
                + "\n numWorkers: " + numWorkers
                + "\n workerIsMultithreaded: " + workerIsMultithreaded
                + "\n combining messages: " + combiningMessages
                + "\n isEc2 run: " + isEc2Run
                );
        
        Job job = new Job( jobName,
                jobDirectoryName, 
                vertexFactory, numParts, workerIsMultithreaded, combiner, workerWriter, 
                workerGraphMaker, reader, writer );
        job.setProblemAggregator( new IntegerSumAggregator() );
        Job[] jobs = { job };
        try
        {
            Client.run( jobs, isEc2Run, numWorkers); //TODO fix this
        } 
        catch ( Exception exception )
        {
            exception.printStackTrace();
            System.exit(1);
        }
        System.exit( 0 );
    }
}
