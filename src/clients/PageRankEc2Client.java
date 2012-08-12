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
        int numParts   = Integer.parseInt( args[1] );
        int numWorkers = Integer.parseInt( args[2] );
//        String imageIdMaster = args[4] ; 
//        String imageIdWorker = args[5] ; 
        
        Job job = new Job( 
                "PageRank",
                args[0],    // jobDirectoryName, 
                new VertexPageRank(), 
                numParts,
                new MasterGraphMakerG1(),
                new WorkerGraphMakerStandard(),
                new MasterOutputMakerStandard(),
                new WorkerOutputMakerStandard()
                );
        System.out.println( job + "\n       numWorkers:" + numWorkers );
        boolean isEc2Master = true;
        Client.run( job, isEc2Master, numWorkers);    
        System.exit( 0 );
    }
}
