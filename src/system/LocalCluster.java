package system;

import java.rmi.RemoteException;

/**
 *
 * @author Pete Cappello
 */
public class LocalCluster extends Cluster
{
    public synchronized ClientToMaster start(int numWorkers) throws RemoteException 
    {
        System.out.println("Cluster.starting.");
        
        master = new LocalMaster();
        master.makeWorkers(numWorkers, null);
        System.out.println("Cluster.start: workers constructed: " + numWorkers);
        return master;
    }

    public void stop()
    {
        try 
        {
            master.shutdown();
        } 
        catch (RemoteException ignore) {} // Expected behavior.
    }
}
