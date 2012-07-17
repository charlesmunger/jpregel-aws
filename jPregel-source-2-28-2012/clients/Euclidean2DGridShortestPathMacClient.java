package clients;

import static java.lang.System.out;
import java.rmi.Naming;

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
public class Euclidean2DGridShortestPathMacClient 
{
    /**
     * @param args the command line arguments
     */
    public static void main( String[] args ) throws RemoteException
    {
        String  jobName               = "Euclidean 2D Grid Shortest Path";
        String  jobDirectoryName      = args[0];
        int     numWorkers            = 2; // should be n * n 
        boolean workerIsMultithreaded = true;
        boolean combiningMessages     = true;
        int     numParts = numWorkers * 2 * 2; // numWorkers * ComputeThreads/Worker * Parts/ComputeThread
        Combiner combiner = null;
        if ( combiningMessages )
        {
            combiner = new FloatMinCombiner();
        }
        Vertex vertexFactory        = new EuclideanShortestPathVertex();
        WorkerWriter workerWriter   = new StandardWorkerOutputMaker();
        GraphMaker workerGraphMaker = new GridWorkerGraphMaker();
        MasterGraphMaker reader     = new GridMasterGraphMaker();
        Writer writer               = new StandardMasterOutputMaker();
        
        out.println("Euclidean2DGridShortestPathMacClient.main: "
                + "\n jobDirectoryName: " + jobDirectoryName
                + "\n numParts: " + numParts
                + "\n numWorkers: " + numWorkers
                + "\n workerIsMultithreaded: " + workerIsMultithreaded
                + "\n combining messages: " + combiningMessages
                );
        
        Job job = new Job( 
                jobName, jobDirectoryName, vertexFactory, numParts, 
                workerIsMultithreaded, combiner, workerWriter, 
                workerGraphMaker, reader, writer 
                );
        job.setProblemAggregator( new IntegerSumAggregator() );
        try
        {
            boolean   isEc2Master = true;
            Client.run( job, isEc2Master, numWorkers); //TODO fix this
        } 
        catch ( Exception exception )
        {
            exception.printStackTrace();
            System.exit(1);
        }
        System.exit( 0 );
//        try
//        {
//            // get reference to Master
//            ClientToMaster master = getMaster();
//            boolean isEc2Master = false;
//            master.setWorkerMap();
//            JobRunData jobRunData = master.run( job, isEc2Master );
//            job.processMasterOutputFile();
//            out.print( jobRunData );
//            master.shutdown();
//        } 
//        catch ( Exception exception )
//        {
//            //exception.printStackTrace();
//            System.exit(1);
//        }
//        System.exit( 0 );
    }
    
    static ClientToMaster getMaster()
    {
        String masterDomainName = "localhost";
        String url = "//" + masterDomainName + ":" + Master.PORT + "/" + Master.SERVICE_NAME;
        ClientToMaster master = null;
        try 
        {
            master = (ClientToMaster) Naming.lookup(url);
        }
        catch (Exception exception) 
        {
            exception.printStackTrace();
            System.exit( 1 );
        }       
        return master;
    }
}
