package JpLAN;

import edu.ucsb.jpregel.system.ClientToMaster;
import edu.ucsb.jpregel.system.FileSystem;
import edu.ucsb.jpregel.system.LocalFileSystem;
import edu.ucsb.jpregel.system.Master;
import java.rmi.AlreadyBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 *
 * @author charlesmunger
 */
public class LANMaster extends Master 
{
    LANMaster() throws RemoteException {}
    
    public static void main(String[] args) throws RemoteException, AlreadyBoundException, InterruptedException 
    {
        System.setSecurityManager(new RMISecurityManager());
        Registry registry = LocateRegistry.createRegistry(PORT);
        ClientToMaster master = new LANMaster();
        registry.bind(SERVICE_NAME, master);
        master.init(Integer.parseInt(args[0]));
        registry.bind(CLIENT_SERVICE_NAME, master);
        System.out.println("LANMaster ready");
    }
    
    @Override
    public void shutdown() { System.exit(0); }

    @Override
    public FileSystem makeFileSystem(String jobDirectoryName) { return new LocalFileSystem(jobDirectoryName); }
}
