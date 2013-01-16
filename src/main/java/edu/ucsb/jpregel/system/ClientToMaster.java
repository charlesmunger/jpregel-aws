package edu.ucsb.jpregel.system;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author Pete Cappello
 */
public interface ClientToMaster extends Remote
{
    void init( int numWorkers) throws RemoteException, InterruptedException;
    
    JobRunData run( Job job) throws RemoteException, InterruptedException;
    
    void setWorkerMap() throws RemoteException, InterruptedException;
    
    void shutdown() throws RemoteException;
}
