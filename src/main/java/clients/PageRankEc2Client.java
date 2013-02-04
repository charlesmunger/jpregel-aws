package clients;

import edu.ucsb.jpregel.system.MasterGraphMakerG1;
import edu.ucsb.jpregel.system.WorkerOutputMakerStandard;
import edu.ucsb.jpregel.system.MasterOutputMakerStandard;
import edu.ucsb.jpregel.system.VertexPageRank;
import edu.ucsb.jpregel.system.WorkerGraphMakerStandard;
import edu.ucsb.jpregel.system.Job;

public class PageRankEc2Client 
{
    public static void main( String[] args ) throws Exception
    {
        int numWorkers = Integer.parseInt( args[1] );        
        Job job = new Job( 
                "PageRank",
                args[0],    // jobDirectoryName, 
                new VertexPageRank(), 
                new MasterGraphMakerG1(),
                new WorkerGraphMakerStandard(),
                new MasterOutputMakerStandard(),
                new WorkerOutputMakerStandard()
                );
        System.out.println( job + "\n       numWorkers:" + numWorkers );
//        Cluster master = Ec2ReservationService.newSmallCluster(numWorkers);
//        master.run( job);    
        System.exit( 0 );
    }
}
