package system;

import java.rmi.RemoteException;

/**
 * Used for development, Master & Workers are instantiated in the client's JVM.
 * 
 * @author Pete Cappello
 */
public class LocalMaster extends Master
{
    private Worker[] workers;
    
    public LocalMaster() throws RemoteException {}
    
    public void constructWorkers( int numWorkers, String masterDomainName ) throws RemoteException
    {
        workers = new Worker[ numWorkers ];
        for ( int workerNum = 0; workerNum < numWorkers; workerNum++ )
        {
            workers[ workerNum ] = new Worker( this );
        }
    }
    
    public void startWorkers( int numWorkers, String masterDomainName ) throws RemoteException {}
    
    public void shutdown() {}
    
    public void stopWorkers() {}
    
    public static void main(String[] args) throws RemoteException
    {
        ClientToMaster master = new LocalMaster();
        System.out.println("LocalMaster: Ready.");
    }
}
