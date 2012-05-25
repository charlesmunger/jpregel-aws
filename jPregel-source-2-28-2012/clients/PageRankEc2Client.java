package clients;

import java.rmi.RemoteException;

import vertex.PageRankVertex;
import masterGraphMakers.G1MasterGraphMaker;
import masterOutputMakers.StandardMasterOutputMaker;
import system.Client;
import system.Combiner;
import system.GraphMaker;
import system.Job;
import system.MasterGraphMaker;
import system.Vertex;
import system.WorkerWriter;
import system.Writer;
import system.combiners.IntegerMinCombiner;
import workerGraphMakers.StandardWorkerGraphMaker;
import workerOutputMakers.StandardWorkerOutputMaker;

public class PageRankEc2Client extends Client {

	public static void main( String[] args ) throws RemoteException
    {
        String    jobName               = "PageRank";
        String    jobDirectoryName      = args[0];
        int       numParts              = Integer.parseInt( args[1] );
        int       numWorkers            = Integer.parseInt( args[2] );
        boolean   workerIsMultithreaded = Boolean.parseBoolean( args[3]);
        boolean   isEc2Master           = true;
        boolean combiningMessages       = Boolean.parseBoolean( args[4]);
        String imageIdMaster = args[5] ; 
        String imageIdWorker = args[6] ; 
        Combiner combiner = null;
        if ( combiningMessages )
        {
            combiner = new IntegerMinCombiner();
        }
        Vertex vertexFactory = new PageRankVertex();
        WorkerWriter workerWriter = new StandardWorkerOutputMaker();
        GraphMaker workerGraphMaker = new StandardWorkerGraphMaker();
        MasterGraphMaker reader = new G1MasterGraphMaker();
        Writer writer = new StandardMasterOutputMaker();
        
        Job job = new Job( jobName,
                jobDirectoryName, 
                vertexFactory, numParts, workerIsMultithreaded, combiner, 
                workerWriter, workerGraphMaker, reader, writer );
        Job[] jobs = { job };
        Client.run( jobs, isEc2Master, numWorkers, imageIdMaster,imageIdWorker );    }
	
	
	
}
