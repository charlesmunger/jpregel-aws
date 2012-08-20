package system;

import JpAws.Ec2Cluster;
import java.rmi.RemoteException;

/**
 * Manages execution of the job on behalf of the application.
 * 
 * @author Pete Cappello
 */
public class Client
{
    /**
     * Runs the job on behalf of the application.
     * 
     * @param job
     * @param isEc2
     * @param numWorkers
     * @return a JobRunData object @see JobRunData
     * @throws RemoteException 
     */
    public static JobRunData run( Job job, boolean isEc2, int numWorkers) throws RemoteException
    {
        Cluster cluster = isEc2 ? new Ec2Cluster() : new LocalCluster();
        ClientToMaster master = cluster.start( numWorkers );
        JobRunData jobRunData = null;
        try
        {
            jobRunData = master.run( job, isEc2 );
            job.processMasterOutputFile();
            System.out.println(jobRunData);
        }
        catch ( Exception exception )
        {
            exception.printStackTrace();
            System.exit( 1 );
        }
        cluster.stop();
        return jobRunData;
    }
}
