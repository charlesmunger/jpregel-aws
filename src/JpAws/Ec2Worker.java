package JpAws;

import java.rmi.RemoteException;
import jicosfoundation.Service;
import system.FileSystem;
import system.Worker;

/**
 *
 * @author charlesmunger
 */
public class Ec2Worker extends Worker {
    Ec2Worker(Service master) throws RemoteException {
        super(master);
    }
    
    public static void main(String[] args) throws RemoteException {
        Worker worker = new Ec2Worker(getMaster(args[0]));
        worker.start();
    }
    
    @Override
    public FileSystem makeFileSystem(String jobDirectoryName) {
        return new S3FileSystem(jobDirectoryName);
    }
}
