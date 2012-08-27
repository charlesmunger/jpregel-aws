package system;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import jicosfoundation.DefaultRemoteExceptionHandler;
import jicosfoundation.Department;
import jicosfoundation.Proxy;
import jicosfoundation.RemoteExceptionHandler;
import jicosfoundation.ServiceImpl;

/**
 *
 * @author Pete Cappello
 */
public class Viewer extends ServiceImpl
{
    // ServiceImpl attributes
    public static String SERVICE_NAME = "VIEWER";
    public final static  int PORT = 2048;
    static private final Department[] departments = { ServiceImpl.ASAP_DEPARTMENT };
    static private final Class[][] command2DepartmentArray =
    {   
        // ASAP Commands
        {
//            ShutdownWorker.class,
//            StartSuperStep.class
        } 
    };
    
    private final static RemoteExceptionHandler REMOTE_EXCEPTION_HANDLER = new DefaultRemoteExceptionHandler(); 
    
    private Proxy masterProxy;
    
    Viewer() throws RemoteException { super( command2DepartmentArray ); }
    
    @Override
    protected void exceptionHandler(Exception exception)
    {
        exception.printStackTrace(); 
        System.exit( 1 );
    }
    
    public synchronized void init() throws RemoteException 
    {
        super.setService(this);
        super.setDepartments(departments);
    }
    
    /**
     */
    public static void main( String[] args ) throws Exception
    {
        System.setSecurityManager( new RMISecurityManager() );
        Registry registry = LocateRegistry.createRegistry(Viewer.PORT);
        Viewer viewer = new Viewer();
        registry.bind(SERVICE_NAME, viewer);
        viewer.init();
        System.out.println("Viewer: Ready.");
    }
}
