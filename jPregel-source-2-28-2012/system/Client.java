package system;

import JpAws.Ec2Cluster;
import java.rmi.RemoteException;

/**
 * @author Pete Cappello
 */
public class Client
{
    public static void run( Job job, boolean isEc2, int numWorkers) throws RemoteException
    {
        System.out.println("Client.run: Entered.");
        Cluster cluster = isEc2 ? new Ec2Cluster() : new LocalCluster();
        System.out.println("Client.run: Cluster object constructed.");
        ClientToMaster master = cluster.start( numWorkers );
        System.out.println("Client.run: begin processing job.");
        try
        {
            JobRunData jobRunData = master.run( job, isEc2 );
            job.processMasterOutputFile();
            System.out.println( jobRunData );
        }
        catch ( Exception exception )
        {
            exception.printStackTrace();
        }
        cluster.stop();
    }
}
