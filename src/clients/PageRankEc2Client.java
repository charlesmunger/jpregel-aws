package clients;

import JpAws.Ec2ReservationService;
import system.*;

public class PageRankEc2Client 
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
        ClientToMaster master = Ec2ReservationService.newSmallCluster(numWorkers);
        master.run( job, isEc2Master);    
        System.exit( 0 );
    }
}
