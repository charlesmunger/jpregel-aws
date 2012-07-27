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
import workerGraphMakers.StandardWorkerGraphMaker;
import workerOutputMakers.StandardWorkerOutputMaker;

public class PageRankEc2Client extends Client
{
    public static void main( String[] args ) throws Exception
    {
        String    jobName               = "PageRank";
        String    jobDirectoryName      = args[0];
        int       numParts              = Integer.parseInt( args[1] );
        int       numWorkers            = Integer.parseInt( args[2] );
        boolean   workerIsMultithreaded = Boolean.parseBoolean( args[3]);
        boolean   isEc2Master           = true;
        String imageIdMaster = args[4] ; 
        String imageIdWorker = args[5] ; 
        Vertex vertexFactory = new PageRankVertex();
        WorkerWriter workerWriter = new StandardWorkerOutputMaker();
        GraphMaker workerGraphMaker = new StandardWorkerGraphMaker();
        MasterGraphMaker reader = new G1MasterGraphMaker();
        Writer writer = new StandardMasterOutputMaker();
        
        Job job = new Job( jobName,
                jobDirectoryName, 
                vertexFactory, numParts, workerIsMultithreaded,  
                workerWriter, workerGraphMaker, reader, writer );
        Client.run( job, isEc2Master, numWorkers);    
        System.exit( 0 );
    }
}
