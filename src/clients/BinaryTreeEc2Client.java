/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clients;

import JpAws.Ec2ReservationService;
import JpAws.PregelAuthenticator;
import api.Cluster;
import com.amazonaws.services.s3.AmazonS3Client;
import java.io.File;
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
//        File clusterFile = new File("cluster.AWS");
        Cluster master;
//        if(clusterFile.exists()) {
//            master = (Cluster) new ObjectInputStream(new FileInputStream(clusterFile)).readObject();
//            master.reset();
//        } else {
            master = Ec2ReservationService.newMassiveCluster(numWorkers);
//            new ObjectOutputStream(new FileOutputStream(clusterFile)).writeObject(master);
//        }
        if(args.length > 2) {
            new AmazonS3Client(PregelAuthenticator.get()).putObject(args[0], "input", new File(args[2]));
        }
        Job job = new Job("Binary Tree Shortest Path",        // jobName
                args[0],                            // jobDirectoryName
                new VertexShortestPathBinaryTree(), // vertexFactory
                new MasterGraphMakerBinaryTree(),  
                new WorkerGraphMakerBinaryTree(),   
                new MasterOutputMakerStandard(),
                new WorkerOutputMakerStandard()                 
                );
        System.out.println( job + "\n    numWorkers: " + numWorkers );
        JobRunData run1 = master.run(job);
        System.out.println(run1);
        Job job2 = new Job("Binary Tree Shortest Path two",        // jobName
                args[0] +"2",                            // jobDirectoryName
                new VertexShortestPathBinaryTree(), // vertexFactory
                new MasterGraphMakerBinaryTree(),  
                new WorkerGraphMakerBinaryTree(),   
                new MasterOutputMakerStandard(),
                new WorkerOutputMakerStandard()                 
                );
        JobRunData run2 = master.run(job2);
        System.out.println(run2);
        Job job3 = new Job("Binary Tree Shortest Path three",        // jobName
                args[0] +"3",                            // jobDirectoryName
                new VertexShortestPathBinaryTree(), // vertexFactory
                new MasterGraphMakerBinaryTree(),  
                new WorkerGraphMakerBinaryTree(),   
                new MasterOutputMakerStandard(),
                new WorkerOutputMakerStandard()                 
                );
        JobRunData run3 = master.run(job3);
        System.out.println(run1);
        System.out.println(run2);
        System.out.println(run3);
        System.exit( 0 );
    }
}
