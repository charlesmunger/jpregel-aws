package JpAws;

import java.rmi.RemoteException;
import jicosfoundation.Service;
import system.FileSystem;
import system.Worker;

/**
 * This class provides a worker that uses S3 for file system access. 
 * @author charlesmunger
 */
public class Ec2Worker extends Worker {
    Ec2Worker(Service master) throws RemoteException {
        super(master);
    }
    
    public static void main(String[] args) throws RemoteException {
        Worker worker = new Ec2Worker(getMaster(args[0]));
        worker.init();
    }
    
    @Override
    public FileSystem makeFileSystem(String jobDirectoryName) {
        return new S3FileSystem(jobDirectoryName);
    }
}
