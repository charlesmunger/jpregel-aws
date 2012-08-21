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
    
//    @Override
//    public void constructWorkers(int numWorkers, String masterDomainName) throws RemoteException
//    {
//        workers = new Worker[numWorkers];
//        for (int workerNum = 0; workerNum < numWorkers; workerNum++)
//        {
//            workers[ workerNum] = new Worker(this);
//            workers[ workerNum].startComputeThreads();
//        }
//    }
    
    public void startWorkers( int numWorkers, String masterDomainName ) throws RemoteException {}
    
    @Override
    public void shutdown() {}
    
    public void stopWorkers() {}
    
    public static void main(String[] args) throws RemoteException
    {
        ClientToMaster master = new LocalMaster();
        master.init(Integer.parseInt(args[0]));
        System.out.println("LocalMaster: Ready.");
    }

    @Override
    public FileSystem makeFileSystem(String jobDirectoryName)
    {
        return new LocalFileSystem(jobDirectoryName);
    }
}
