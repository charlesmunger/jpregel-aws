package system;

import java.rmi.RemoteException;
import jicosfoundation.Service;

/**
 *
 * @author charlesmunger
 */
public class LocalWorker extends Worker {

    public LocalWorker(Service master) throws RemoteException
    {
        super(master);
    }

    @Override
    public FileSystem makeFileSystem(String jobDirectoryName)
    {
       return new LocalFileSystem(jobDirectoryName);
    }

}
