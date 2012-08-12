package clients;

import api.MasterOutputMaker;
import api.MasterGraphMaker;
import api.WorkerGraphMaker;
import api.WorkerOutputMaker;
import system.MasterGraphMakerG1;
import system.MasterOutputMakerStandard;
import system.*;
import system.VertexPageRank;
import system.WorkerGraphMakerStandard;
import system.WorkerOutputMakerStandard;

public class PageRankEc2Client extends Client
{
    public static void main( String[] args ) throws Exception
    {
        String    jobName               = "PageRank";
        String    jobDirectoryName      = args[0];
        int       numParts              = Integer.parseInt( args[1] );
        int       numWorkers            = Integer.parseInt( args[2] );
        boolean   isEc2Master           = true;
        String imageIdMaster = args[4] ; 
        String imageIdWorker = args[5] ; 
        VertexImpl vertexFactory = new VertexPageRank();
        WorkerOutputMaker workerWriter = new WorkerOutputMakerStandard();
        WorkerGraphMaker workerGraphMaker = new WorkerGraphMakerStandard();
        MasterGraphMaker reader = new MasterGraphMakerG1();
        MasterOutputMaker writer = new MasterOutputMakerStandard();
        
        Job job = new Job( jobName,
                jobDirectoryName, 
                vertexFactory, numParts,  
                workerWriter, workerGraphMaker, reader, writer );
        Client.run( job, isEc2Master, numWorkers);    
        System.exit( 0 );
    }
}
