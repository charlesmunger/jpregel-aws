package JpAws;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
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
    private WorkerMachines workerMachines;
    
    Ec2Master() throws RemoteException
    {}
    
    public static void main(String[] args) throws RemoteException, AlreadyBoundException 
    {
        System.setSecurityManager(new RMISecurityManager());
        Registry registry = LocateRegistry.createRegistry(Master.PORT);
        ClientToMaster master = new Ec2Master();
        registry.bind(SERVICE_NAME, master);
        System.out.println("Ec2Master: Ready.");
    }
    
    @Override
    public void shutdown() 
    {       
        System.out.println("Master.shutdown: notifying Worker Services to shutdown.");
        // shutdown all Worker Services
        try 
        {
            workerMachines.Stop();
        } 
        catch (IOException ex) 
        {
            System.out.println("Exception shutting down workers. Check webUI for zombie instances.");
        }
        System.out.println("Master.shutdown: Worker Services shutdown.");

        // shutdown Master
        System.out.println("Master.shutdown: shutting down.");
    }

    @Override
    public FileSystem makeFileSystem(String jobDirectoryName)
    {
        return new S3FileSystem(jobDirectoryName);
    }
}
