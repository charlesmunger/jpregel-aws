package clients;

import JpLAN.LANReservationService;
import edu.ucsb.jpregel.system.Job;
import edu.ucsb.jpregel.system.MasterGraphMakerBinaryTree2;
import edu.ucsb.jpregel.system.MasterOutputMakerStandard;
import edu.ucsb.jpregel.system.WorkerGraphMakerBinaryTree2;
import edu.ucsb.jpregel.system.WorkerOutputMakerStandard;
import vertices.VertexSsspBinaryTree;

/**
 * Test SsspBinaryTree client, where each worker resides in a separate JVM.
 * @author Pete Cappello
 */
public class SsspBinaryTreeLan 
{
    /**
     * Run an SSSP problem on a binary tree in a LAN.
     * @param args 0 - Job directory path or bucket name
     *             1 - number of workers
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception 
    {
        int numWorkers = Integer.parseInt( args[1] );
        Job job = new Job( "SSSP on binary tree on a LAN", // jobName
                  args[0],                        // jobDirectoryName
                  new VertexSsspBinaryTree(),     // vertexFactory
                  new MasterGraphMakerBinaryTree2(),  
                  new WorkerGraphMakerBinaryTree2(),   
                  new MasterOutputMakerStandard(),
                  new WorkerOutputMakerStandard() 
                  );
        System.out.println( job + "\n         numWorkers: " + numWorkers );
        System.out.println( LANReservationService.newLocalCluster( numWorkers ).run( job ) );
        System.exit(0);
    }
}
