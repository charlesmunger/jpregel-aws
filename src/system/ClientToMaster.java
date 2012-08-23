package system;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author Pete Cappello
 */
public interface ClientToMaster extends Remote
{
    void init( int numWorkers) throws RemoteException;
    
    JobRunData run( Job job) throws RemoteException;
    
    void setWorkerMap() throws RemoteException;
    
    void shutdown() throws RemoteException;
}
