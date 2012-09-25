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
 * The ProxyManager maintains a Map of Proxy objects, keyed by Service.
 * ServiceImpl asks it to route a List of Command objects to the sending 
 * Service's Proxy. If there is no such Proxy, it sends the Command List to
 * the visitorProxy.
 *
 * ProxyManager can and should be constructed with an initialCapacity,
 * loadFactor, and concurrencyLevel (see ConcurrentHashMap).
 *
 * @author  Peter Cappello
 */

package jicosfoundation;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class ProxyManager extends ConcurrentHashMap<Service,Proxy> 
//public class ProxyManager extends Hashtable<Service,Proxy> 
{        
    public ProxyManager(int capacity, float loadFactor,int concurrencyLevel) 
    {
        super(capacity,loadFactor,concurrencyLevel);
    }
    
    // !! eliminate
    public void addProxy( ServiceName serviceName, Proxy proxy )
    {
        assert proxy != null;        
        put( serviceName.service(), proxy );
    }
    
    public void addProxy( Service service, Proxy proxy )
    {
        assert proxy != null;
        put( service, proxy );
    }
    
    // @return may be null.
    public Proxy getProxy( Service service )
    {
        assert service != null;
        return get( service );
    }
        
    public Proxy removeProxy( Service service ) 
    { 
        assert service != null;        
        return remove( service ); 
    }
    
    /** Some Command objects are sent to all Proxy objects, not just a single 
     * Proxy. The ProxyManager performs this service.
     */
    public void broadcast( Command command, Service fromService )
    {
        assert command != null;
        for ( Iterator<Proxy> i = values().iterator(); i.hasNext(); )
        {
            Proxy proxy =  i.next();
            Service toService = proxy.remoteService();
            if ( fromService == null || ! fromService.equals( toService ) )
            {
                proxy.execute( command );
            }
        }
    }
}
