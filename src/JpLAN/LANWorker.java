package JpLAN;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import jicosfoundation.Service;
import system.LocalWorker;
import system.Worker;

/**
 *
 * @author charlesmunger
 */
public class LANWorker extends LocalWorker {

    private LANWorker(Service master) throws RemoteException
    {
        super(master);
    }
        
    /*
     * Invoke to deploy a Worker on some machine
     * 
     * @param args [0]: Domain Name of machine on which Master is running
     */
    public static void main( String[] args ) throws RemoteException
    {
        Worker worker = new LANWorker(getMaster( args[0]));
        worker.init();
        System.out.println( "Worker: Ready." );
    }
}
