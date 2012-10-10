package clients;

import JpAws.Ec2ReservationService;
import JpAws.PregelAuthenticator;
import api.Cluster;
import com.amazonaws.services.s3.AmazonS3Client;
import java.io.File;
import system.Job;
import system.JobRunData;
import system.MasterGraphMakerBinaryTree;
import system.MasterOutputMakerStandard;
import system.WorkerGraphMakerBinaryTree;
import system.WorkerOutputMakerStandard;
import vertices.VertexShortestPathBinaryTree;

/**
 *
 * @author Pete Cappello
 */
public class ClientSsspBinaryTreeAws
{
    /**
     * @param args[0]: Job directory name
     *        args[1]: Number of workers
     *        args[2]: File name (path relative to project)
     */
    public static void main( String[] args ) throws Exception
    {
        int numWorkers = Integer.parseInt( args[1] );  
        Cluster master = Ec2ReservationService.newMassiveCluster(numWorkers);
        if( args.length > 2 ) 
        {
            new AmazonS3Client(PregelAuthenticator.get()).putObject( args[0], "input", new File( args[2] ) );
        }
        Job job = new Job("Binary Tree Shortest Path",  // jobName
                args[0],              // jobDirectoryName (S3 bucket name)
                new VertexShortestPathBinaryTree(),     // vertexFactory
                new MasterGraphMakerBinaryTree(),  
                new WorkerGraphMakerBinaryTree(),   
                new MasterOutputMakerStandard(),
                new WorkerOutputMakerStandard()                 
                );
        JobRunData jobRunData = master.run( job );
        System.out.println( jobRunData );
        master.terminate();
        System.exit( 0 );
    }
}
