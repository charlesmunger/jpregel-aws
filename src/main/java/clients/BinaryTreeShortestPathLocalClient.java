package clients;

import edu.ucsb.jpregel.system.WorkerOutputMakerStandard;
import edu.ucsb.jpregel.system.MasterOutputMakerStandard;
import edu.ucsb.jpregel.system.ClientToMaster;
import edu.ucsb.jpregel.system.WorkerGraphMakerBinaryTree;
import edu.ucsb.jpregel.system.MasterGraphMakerBinaryTree;
import edu.ucsb.jpregel.system.Job;
import edu.ucsb.jpregel.system.VertexShortestPathBinaryTree;
import edu.ucsb.jpregel.system.LocalReservationService;

/**
 *
 * @author Pete Cappello
 */
public class BinaryTreeShortestPathLocalClient 
{  
    /**
     * @param args [0]: Job directory name
     */
    public static void main( String[] args ) throws Exception
    {
        int numWorkers = Integer.parseInt(args[1]);  
        Job job = new Job("Binary Tree Shortest Path",        // jobName
                args[0],                            // jobDirectoryName
                new VertexShortestPathBinaryTree(), // vertexFactory
                new MasterGraphMakerBinaryTree(),  
                new WorkerGraphMakerBinaryTree(),   
                new MasterOutputMakerStandard(),
                new WorkerOutputMakerStandard()                 
                );
        System.out.println( job + "\n    numWorkers: " + numWorkers );
        ClientToMaster master = LocalReservationService.newCluster(numWorkers);
        System.out.println(master.run(job));
        
        System.exit( 0 );
    }
}
