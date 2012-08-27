/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clients;

import JpAws.Ec2ReservationService;
import api.Cluster;
import system.*;

/**
 *
 * @author Charles
 */
public class BinaryTreeEc2Client
{
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
        Cluster master = Ec2ReservationService.newMassiveCluster(numWorkers);
        System.out.println(master.run(job));
        System.exit( 0 );
    }
}
