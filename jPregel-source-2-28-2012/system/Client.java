package system;

import static java.lang.System.out;

import java.rmi.RemoteException;

/**
 *
 * @author Pete Cappello
 */
public class Client
{
	public static void run( Job[] jobs, boolean isEc2, int numWorkers , String imageIdMaster , String imageIdWorker ) 
            throws RemoteException
    {
        System.out.println("Client.run: entered.");
        Cluster cluster = new Cluster();
        ClientToMaster master = cluster.start( isEc2, numWorkers, imageIdMaster, imageIdWorker );
        for ( Job job : jobs )
        {
            System.out.println("Client.run: begin processing job.");
            JobRunData jobRunData = master.run( job, isEc2 );
            job.processMasterOutputFile();
            out.print( jobRunData );
        }
        cluster.stop( isEc2 );
    }
}
