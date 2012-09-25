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
 * !! 1. modify Proxy in jicosfoundation to use only "Service" constructor
 *
 * @author  Peter Cappello
 */

package system;

import jicosfoundation.DefaultRemoteExceptionHandler;
import jicosfoundation.Proxy;
import jicosfoundation.RemoteExceptionHandler;
import jicosfoundation.Service;

public final class ProxyWorker extends Proxy 
{
    // constants
    private static final long TERM = 1000 * 5; // 5 seconds
    private final static RemoteExceptionHandler REMOTE_EXCEPTION_HANDLER 
                                          = new DefaultRemoteExceptionHandler();        
    // Jicos attributes
    private Service workerService;
    
    // ProxyWorker attributes
//    private static int BUFFER_SIZE = 10;
//    private BlockingQueue q = new LinkedBlockingQueue( BUFFER_SIZE );
    
    ProxyWorker( Service workerService, Master master, 
                 RemoteExceptionHandler remoteExceptionHandler ) 
    {
        super ( workerService, master, remoteExceptionHandler, TERM );
        this.workerService = workerService;
    }
    
    ProxyWorker( Service workerService, Worker sourceWorker, 
                 RemoteExceptionHandler remoteExceptionHandler ) 
    {
        super ( workerService, sourceWorker, remoteExceptionHandler, TERM );
        this.workerService = workerService;
    }
    
    @Override
    public void evict() 
    {
        if ( ! kill )
        {
            super.kill(); // kill "ping" thread
            System.out.println("ProxyWorker.evict: killed Proxy: "
                    + " workerService: " + workerService );
        }
        else
        {
            System.out.println("WorkerProxy.evict: Thread already evicted.");
        }
    }
    
    Service worker() { return workerService; }
}
