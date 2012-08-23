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
    
    @Override
    public void shutdown() {}

    @Override
    public FileSystem makeFileSystem(String jobDirectoryName)
    {
        return new LocalFileSystem(jobDirectoryName);
    }
}
