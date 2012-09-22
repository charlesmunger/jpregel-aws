/* ************************************************************************* *
 *                                                                           *
 *        Copyright (c) 2004 Peter Cappello  <cappello@cs.ucsb.edu>          *
 *                                                                           *
 *    Permission is hereby granted, free of charge, to any person obtaining  *
 *  a copy of this software and associated documentation files (the          *
 *  "Software"), to deal in the Software without restriction, including      *
 *  without limitation the rights to use, copy, modify, merge, publish,      *
 *  distribute, sublicense, and/or sell copies of the Software, and to       *
 *  permit persons to whom the Software is furnished to do so, subject to    *
 *  the following conditions:                                                *
 *                                                                           *
 *    The above copyright notice and this permission notice shall be         *
 *  included in all copies or substantial portions of the Software.          *
 *                                                                           *
 *    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,        *
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF       *
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.   *
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY     *
 *  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,     *
 *  TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE        *
 *  SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                   *
 *                                                                           *
 * ************************************************************************* */

/**
 * Generic distributed service.
 *
 * @version 1.0
 * @author  Peter Cappello
 */

/*
 * Extender of ServiceImpl: Your constructor must invoke setDepartments, passing
 * it an array of Department objects, the 0th of which is assumed to be the asap
 * "Department": A Command type is Associate with this "Department" if and only 
 * if the Command types is to be executed by the RMI thread executing the 
 * receiveCommands method. 
 * Such Command types must invoke neither wait() nor Remote methods.
 */

package jicosfoundation;

import java.io.*;
import java.rmi.*;
import java.rmi.server.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

abstract public class ServiceImpl extends UnicastRemoteObject 
                                  implements Service
{
    // Constants
    public  static final Department ASAP_DEPARTMENT = null;
    private static final int ASAP_DEPARTMENT_NUM = 0;    
    
    // Standard ServiceImpl parts
    private ServiceImpl myService; // The service that extends me.
    private ServiceName serviceName;
    private Department[] departments; 
    private Class2Int command2Department;
    private ProxyManager proxyManager = new ProxyManager( 100, 0.75f, 1 );
//    private ProxyManager proxyManager = new ProxyManager();
//    private BlockingQueue readyMailQ = new LinkedBlockingQueue();
    private Set registry = Collections.synchronizedSet( new HashSet() );
    
    /** Constructor.
     * @throws RemoteException Since ServiceImpl implements Service, a Remote 
     * interface, its no-arg constructor
     * throws RemoteException.
     * @param command2DepartmentArray is an array of Class arrays. It has a 
     * Class[] for each Department. Each Class[] has a Class for each Command 
     * processed by the Department. Class[0] is assumed to be the set of Command
     * objects that are processed asap (within the RMI-Thread: w/o 
     * Thread-switching). See the processMail method.
     */
    protected ServiceImpl( Class[][] command2DepartmentArray ) 
           throws RemoteException
    {
        serviceName = new ServiceName ( this );
        command2Department = new Class2Int ( command2DepartmentArray );
    }

    /** Add a Mailer for this Service (Remote reference).
     * @param address a Remote reference to the destination Service.
     * @return a reference to the Mailer object just created. 
     * Client may cache this for subsequent use.
     */
    protected synchronized final Mailer addMail( Proxy myProxy,
                                 RemoteExceptionHandler remoteExceptionHandler ) 
    {
        assert remoteExceptionHandler != null;        
        return new Mailer( this, remoteExceptionHandler, new LinkedBlockingQueue(), myProxy );
    }
    
    // !! eliminate
    public final void addProxy( ServiceName serviceName, Proxy proxy ) 
    { 
        assert serviceName != null;
        assert proxy != null;       
        proxyManager.addProxy( serviceName, proxy ); 
    }
    
    public final void addProxy( Service service, Proxy proxy ) 
    { 
        assert service != null;
        assert proxy   != null;       
        proxyManager.addProxy( service, proxy ); 
    }
    
    public void broadcast( Command command, Service fromService )
    {
        assert command != null;        
        for ( Iterator i = proxyManager.values().iterator(); i.hasNext(); )
        {
            Proxy proxy = (Proxy) i.next();
            if ( fromService == null || ! fromService.equals( proxy.getService() ) )
            {
                proxy.execute( command );
            }            
        }
    }
    
    public void broadcast( Command command, Service fromService, Collection proxies )
    {
        assert command != null;
        assert proxies != null;       
        for ( Iterator i = proxies.iterator(); i.hasNext(); )
        {
            Proxy proxy = (Proxy) i.next();
            if ( fromService == null || ! fromService.equals( proxy.getService() ) )
            {
                proxy.execute( command );
            }            
        }
    }
    
    protected synchronized final void clean() 
    {
        /* Remove all Mail objects from transient addresses that are inactive. 
         * What event prompts its invocation?
         * "WSClock - A Simple and Effective Algorithm for Virtual 
         * Memory Management", Carr and Hennessey, Proc. 8th SOSP, Operating 
         * Systems, Review, 15(5), Dec. 1981.
         */
    }
    
    abstract protected void exceptionHandler( Exception exception );
    
    /** Implementation of the Service execute Remote method.
     * @param command The Command object to be executed remotely.
     * @return the object that is returned by the Command's execute method. 
     * This depends on the actual Command type.
     */    
    @Override
    public final Object executeCommand( Service sender, CommandSynchronous command ) 
    {
        return command.execute( myService );
    }
    
    public Proxy getProxy( Service service )
    {
        assert service != null;        
        return proxyManager.getProxy( service );
    }
    
    private void processCommands( Queue<Command> q) throws Exception
    {          
        int department$ = 0;       
        for ( Iterator<Command> i = q.iterator(); i.hasNext(); )
        {
            Command command = i.next();
            if ( command instanceof CommandList )
            {
                Queue<Command> queue = ((CommandList) command).q();
                processCommands ( queue );
            }
            else
            {
                try
                {
                    department$ = command2Department.map( command );
                }
                catch ( IllegalArgumentException e )
                {
                    System.err.println("Unmapped Command: " + command.getClass() + " for " + myService.getClass() );
                    System.exit(1);
                }
                if ( department$ == ASAP_DEPARTMENT_NUM ) 
                {     
                    command.execute( this ); // Asap Command  
                }
                else 
                {
                    departments[ department$ ].addCommand( command );
                }   
            }
        }
    }
    
    protected ProxyManager proxyManager() { return proxyManager; }
    
    /** Implements the Service interface deliver method. 
     * It is internal to the foundation package. See the Service interface.
     * @param marshalledCommandQ See the Service interface.
     */    
    @Override
    public final void receiveCommands ( Service sender, Queue<Command> commandQ)
    {
        assert sender != null;
        assert commandQ != null;
        try
        {
            processCommands ( commandQ );
        }
        catch ( Exception exception )
        {
            System.out.println("ServiceImpl.receiveCommands: exception");
            exception.printStackTrace();
            System.exit( 1 );
        }
    }
    
    protected void register ( Service service ) { registry.add ( service ); }
    
    protected Proxy removeProxy( Service service ) 
    { 
        assert service != null;
        return proxyManager.removeProxy( service ); 
    }
    
    // !! currently assuming Proxy was put in proxyManager and was not removed.
    public void sendCommand( Service service, Command command )
    {
        assert service != null;
        Proxy proxy = proxyManager.getProxy( service );
        proxy.execute( command );
    }
    
    public ServiceName serviceName() { return serviceName; }
    
    /** Departments must be set by extensions using setDepartment. This
     * typically is done in the extender's constructor, after invoking super.
     * @param departments an array of Department objects. The 0th element is 
     * omitted, since this "Department" refers to the "ASAP_DEPARTMENT": 
     * Command objects associated with this "Department" are executed by the RMI
     * thread that implements the deliver method.
     */
    protected final void setDepartments( Department[] departments ) 
    {
        this.departments = departments; 
    }
    
    /** A reference to an object that extends ServiceImpl, such as a Host 
     * object. This is needed so that Commands executed in the RMI deliver 
     * thread will have access to the Host's state, for example.
     * @param myService reference to an object that extends ServiceImpl, such as
     * a Host object.
     */    
    protected void setService ( ServiceImpl myService ) 
    {
        assert myService != null;       
        this.myService = myService; 
    }
    
    public void shutdown(){ System.out.println("ServiceImpl.shutdown: wrong actual object");}
    
    protected void unregister ( Service service ) { registry.remove ( service ); }
    
    protected static byte[] objectToBytes( Object object ) throws java.io.IOException 
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream( byteArrayOutputStream );
        objectOutputStream.writeObject( object );
        objectOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }
    
    protected static Object bytesToObject( byte[] bytes ) 
              throws java.io.IOException, ClassNotFoundException
    {        
        if ( bytes == null ) 
        {
            return null;
        }
        ObjectInputStream objectInputStream = new ObjectInputStream( new ByteArrayInputStream( bytes ) );
        return objectInputStream.readObject();
    }
}
