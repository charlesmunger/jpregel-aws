package clients;

import edu.ucsb.jpregel.system.ClientToMaster;
import edu.ucsb.jpregel.system.Job;
import edu.ucsb.jpregel.system.LocalReservationService;
import edu.ucsb.jpregel.system.MasterGraphMakerBinaryTree2;
import edu.ucsb.jpregel.system.MasterOutputMakerStandard;
import edu.ucsb.jpregel.system.WorkerGraphMakerBinaryTree2;
import edu.ucsb.jpregel.system.WorkerOutputMakerStandard;
import vertices.VertexSsspBinaryTree;

/**
 * An SSSP binary tree client that uses a "local" cluster.
 * 
 * @author Pete Cappello
 */
public class SsspBinaryTree
{
    /**
     * @param args[0]: local job directory name
     *        args[1]: number of worker machines
     */
    public static void main( String[] args ) throws Exception
    {
        int numWorkers = Integer.parseInt( args[1] );
        Job job = new Job("Binary Tree Shortest Path",  // jobName
                args[0],
                new VertexSsspBinaryTree(),     // vertexFactory
                new MasterGraphMakerBinaryTree2(),  
                new WorkerGraphMakerBinaryTree2(),   
                new MasterOutputMakerStandard(),
                new WorkerOutputMakerStandard()                 
                );
        System.out.println("SsspBinaryTree: " + job );
        ClientToMaster master = LocalReservationService.newCluster( numWorkers );
        System.out.println(master.run(job));
        System.exit( 0 );
    }
}
