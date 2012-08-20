package clients;

import system.*;

public class PageRankEc2Client extends Client
{
    public static void main( String[] args ) throws Exception
    {
        int numParts   = Integer.parseInt( args[1] );
        int numWorkers = Integer.parseInt( args[2] );        
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
