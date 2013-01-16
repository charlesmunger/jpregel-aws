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
 * Immutable object that has the Service object's name information. 
 *
 * @author  Peter Cappello
 */

package jicosfoundation;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class ServiceName implements java.io.Serializable
{
    private Service service;
    private String iPAddress;
    private String domainName;
    
    public ServiceName( Service service )
    {
        // pre-condition
        assert service != null;
        
        setNetworkAttributes( service );
    }    
    
    /** A copy constructor.
     * @param serviceName The service name to copy.
     */    
    public ServiceName( ServiceName serviceName )
    {
        service    = serviceName.service();
        iPAddress  = serviceName.ipAddress();
        domainName = serviceName.domainName();
    }        
    
    /** Returns the Service's IP address.
     * @return the Service's IP address.
     */    
    public final String ipAddress()  { return iPAddress; }
    
    /** Returns the Service's domain name.
     * @return the Service's domain name.
     */    
    public final String domainName() { return domainName; }
    
    /** Not used by Jicos applications.
     * @return Not used by Jicos applications.
     */    
    public final Service service()   { return service; }
    
    private void setNetworkAttributes( Service service )
    {
        // pre-condition
        assert service != null;
        
        this.service = service;
        try
        {
            iPAddress  = InetAddress.getLocalHost().getHostAddress();
            domainName = InetAddress.getLocalHost().getCanonicalHostName();        
        }
        catch ( UnknownHostException e )
        {
            iPAddress = "UnknownHost";
            domainName = "UnknownHost";
        }
    }
    
    /** Returns a String representation of the object.
     * @return a String representation of the ID address and domain name.
     */    
    public String toString() 
    { 
        return iPAddress + "(" + domainName + ") ";
    }
    
    public String toStringWithSpace() {
    	    return( iPAddress + " (" + domainName + ")" );
    }
}
