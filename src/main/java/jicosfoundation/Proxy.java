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
 * A Remote proxy: A Service interacts with a Remote Service via its Proxy.
 *
 * @author  Peter Cappello
 */

/*
 * !! One thread suffices to service ALL Proxy objects with respect to Lease 
 *    expiration/renewal.
 *
 * The Proxy also is responsible for maintaining state information pertaining to
 * the Service for which it is a proxy. Even Remote method invocations on its
 * containing Service from the Service which it serves as Proxy may need to 
 * invoke proxy methods to maintain state. For example, a Host may send a
 * TaskServer a ProcessResult command. The Host's proxy must update its state to
 * reflect that the actual Host has completed some particular Task and update 
 * Task execution-time statistics for the actual Host.
 *
 * A Proxy also is a Thread-safe lease manager (evict, offerRenewal, renew).
 */

package jicosfoundation;

import java.rmi.*;

public abstract class Proxy extends Thread 
{
    // constants
    private static final CommandSynchronous PING = new Ping();
    
    // attributes
    private ServiceName serviceName; // !! eliminate
    private Service service; // !! remove either service or remoteService
    private ServiceImpl containingServiceImpl;
    protected boolean kill;
    
    private RemoteExceptionHandler remoteExceptionHandler;
    private Mailer mailer;
      
    private boolean anInternalService;
    private boolean isUnreachable = false;
    //private Proxy forwardingProxy;
    
    // convenience
    private Service remoteService;
    
    // !! part of LeaseManager
    private long term; 
    private long expirationTime;
    private long remainingTime;
    
    
    // !! When all components are Proxified, remove Mail object from Service
    // Put in Proxies ??
    public Proxy( ServiceName serviceName, 
                  ServiceImpl containingServiceImpl,
                  RemoteExceptionHandler remoteExceptionHandler,
                  long term
                ) 
    {
        super( "Jicos Proxy for " + serviceName );
        
        assert serviceName != null;
        
        this.serviceName = serviceName;
        service = serviceName.service();
        constructProxy(service, containingServiceImpl, remoteExceptionHandler, term);
    }
    
    public Proxy( Service service, ServiceImpl containingServiceImpl,
                  RemoteExceptionHandler remoteExceptionHandler, long term
                ) 
    {        
        assert service != null;
        
        constructProxy(service, containingServiceImpl, remoteExceptionHandler, term);
    }
    
    void constructProxy( Service service, ServiceImpl containingServiceImpl,
                        RemoteExceptionHandler remoteExceptionHandler, long term
                       ) 
    {        
        assert service != null;
        
        this.service = service;
        this.containingServiceImpl = containingServiceImpl;
        this.remoteExceptionHandler = remoteExceptionHandler;
        this.term = term;
        // !! after proxified, use Mail constructor.
        
        remoteService = service;
        if ( containingServiceImpl != null )
        {
            // Proxy for a Remote Service
            mailer = containingServiceImpl.addMail( this, remoteExceptionHandler );
        }
        else
        {
            // Proxy for an internal Service            
            anInternalService = true;
        }
        renew();
        start();
    }
    
    public void containingServiceImpl( ServiceImpl containingServiceImpl )
    {
        assert containingServiceImpl != null;        
        this.containingServiceImpl = containingServiceImpl;
    }
    
    abstract public void evict();
    
    public Object execute( CommandSynchronous command,
                           RemoteExceptionHandler remoteExceptionHandler )
    {
        assert remoteExceptionHandler != null;
        return command.execute( this, remoteExceptionHandler );
    }
    
    public void execute( Command command ) { command.execute( this ); }
    
    public Service getService() { return service; }
    
    public boolean isUnreachable() { return isUnreachable; }
    
    public synchronized void kill() { kill = true; }
    
    public boolean offerRenewal()
    {                
        if ( containingServiceImpl == null )
        {
            // internal Host: Ignore
            renew();
            return true;
        }        
	try 
        {
            remoteService.executeCommand(containingServiceImpl, PING);
	} 
        catch ( RemoteException e ) 
        {
            System.out.println("Proxy.offerRenewal: PING RemoteException.");
            e.printStackTrace();
            LogManager.getLogger(this).log( LogManager.INFO,
		 "RemoteException (" + e.getCause().getClass().getName() + ")");
            return false; // remoteService is not responding.
	}    
//        catch ( Exception e )
//        {
//            System.out.println("Proxy.offerRenewal: PING Exception.");
//            e.printStackTrace();
//            System.exit( 1 );
//        }
        renew();
        return true;
    }     
    
    public void remoteExceptionHandler( RemoteExceptionHandler remoteExceptionHandler )
    {
        assert remoteExceptionHandler != null;        
        this.remoteExceptionHandler = remoteExceptionHandler;
    }
    
    public Service remoteService() { return remoteService; }
    
    public synchronized void renew()
    {
        expirationTime = System.currentTimeMillis() + term;
        remainingTime = term;
    }
    
    public void run()
    {
        while ( true )
        {
            try
            {
                sleep ( remainingTime );
            }   
            catch ( InterruptedException ignore ) {}
            
            if ( kill )
            {
                System.out.println("Proxy.run: exiting ... (killing thread)");
                return; // kill this thread
            }
            
            synchronized ( this )
            {
                remainingTime = expirationTime - System.currentTimeMillis();           
                if ( remainingTime < 0 && ! offerRenewal() )
                {
                    System.out.println("Proxy.run: about to invoke evict. remainingTime: " + remainingTime );
                    evict();
                    return; // kill thread
                }
            }            
        }
    }
    
    public void sendCommand( Command command )
    { 
        assert command != null;        
        if ( anInternalService )
        {
            try
            {
                command.execute( (ServiceImpl) remoteService );
            }
            catch ( Exception exception )
            {
                containingServiceImpl.exceptionHandler( exception );
            }
            return;
        }
        mailer.add( command );
    }
    
    public ServiceName serviceName() { return serviceName; }
    
    public void setTerm( long term ) 
    {
        assert term > 0;        
        this.term = term; 
    }
    
    public void unreachable() { isUnreachable = true; }
}
