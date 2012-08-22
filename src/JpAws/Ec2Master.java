package JpAws;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import system.ClientToMaster;
import system.FileSystem;
import system.Master;


/**
 *
 * @author Charles Munger
 */
public class Ec2Master extends Master
{
    
    Ec2Master() throws RemoteException
    {}
    
    public static void main(String[] args) throws Exception
    {
        System.setSecurityManager(new RMISecurityManager());
        Registry registry = LocateRegistry.createRegistry(Master.PORT);
        ClientToMaster master = new Ec2Master();
        registry.bind(SERVICE_NAME, master);
        master.init(Integer.parseInt(args[0]));
        System.out.println("About to bind");
        registry.bind(CLIENT_SERVICE_NAME, master);
        System.out.println("Ec2Master: Ready.");
    }

    @Override
    public FileSystem makeFileSystem(String jobDirectoryName)
    {
        return new S3FileSystem(jobDirectoryName);
    }
}
