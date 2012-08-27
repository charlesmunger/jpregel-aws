package system;

import java.rmi.RemoteException;

/**
 *
 * @author charlesmunger
 */
public class LocalWorker extends Worker {

    public LocalWorker(Master master) throws RemoteException
    {
        super(master);
    }

    @Override
    public FileSystem makeFileSystem(String jobDirectoryName)
    {
       return new LocalFileSystem(jobDirectoryName);
    }

}
