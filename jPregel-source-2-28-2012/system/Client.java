package system;

import JpAws.Ec2Cluster;
import static java.lang.System.out;

import java.rmi.RemoteException;

/**
 *
 * @author Pete Cappello
 */
public class Client
{
    public static void run( Job[] jobs, boolean isEc2, int numWorkers) 
            throws RemoteException
    {
        out.println("Client.run: Entered.");
        Cluster cluster = isEc2 ? new Ec2Cluster() : new LocalCluster();
        System.out.println("Client.run: Cluster object constructed.");
        ClientToMaster master = cluster.start( numWorkers );
        for ( Job job : jobs )
        {
            System.out.println("Client.run: begin processing job.");
            JobRunData jobRunData = master.run( job, isEc2 );
            job.processMasterOutputFile();
            out.print( jobRunData );
        }
        cluster.stop();
    }
}
