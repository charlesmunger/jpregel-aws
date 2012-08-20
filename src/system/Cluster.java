package system;

import java.rmi.RemoteException;

/**
 * start & stop a Master and set of Workers
 *
 * @author Pete Cappello
 */
abstract public class Cluster 
{
    protected ClientToMaster master;
    
    abstract public ClientToMaster start(int numWorkers) throws RemoteException; 

    abstract public void stop(); 
}