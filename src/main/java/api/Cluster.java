package api;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;
import edu.ucsb.jpregel.system.Job;
import edu.ucsb.jpregel.system.JobRunData;

/**
 *
 * @author Pete Cappello
 */
public interface Cluster extends Remote
{
    public static final int PORT = 5000;
    public static final String SERVICE_NAME = "Cluster";
        
    void register() throws RemoteException;
    
    void reset() throws RemoteException, ExecutionException, InterruptedException, IOException;
    
    JobRunData run( Job job ) throws RemoteException, InterruptedException;
    
    JobRunData run( Job job, String localFilePathName ) throws RemoteException, InterruptedException;
    
    void terminate() throws RemoteException, IOException;
}
