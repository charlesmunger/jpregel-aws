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
        System.out.println(master.run(job));
        System.exit( 0 );
    }
}
