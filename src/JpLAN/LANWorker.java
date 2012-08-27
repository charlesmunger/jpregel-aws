package JpLAN;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import system.LocalWorker;
import system.Master;
import system.Worker;

/**
 *
 * @author charlesmunger
 */
public class LANWorker extends LocalWorker {

    private LANWorker(Master master) throws RemoteException
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
        System.setSecurityManager( new RMISecurityManager() );
        
        // get reference to Master
        if ( 1 != args.length )
        {
            System.out.println("java " + Worker.class.getName() + " MasterDomainName");
        }
        String masterDomainName = args[0];
        Master master = getMaster( masterDomainName );          
        Worker worker = new LANWorker(master);
        worker.init();
        System.out.println( "Worker: Ready." );
    }
}
