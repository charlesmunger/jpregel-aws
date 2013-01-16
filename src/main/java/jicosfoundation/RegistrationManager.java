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
 * Manages the registration of Service objects into an rmiregistry
 *
 * Created on December 3, 2003, 9:39 AM
 *
 * @author  Peter Cappello
 */

package jicosfoundation;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import static java.lang.System.*;


public final class RegistrationManager 
{
    public static final int PORT = 5237;
    
    public static Registry locateRegistry()
    {
        Registry registry = null;
        try
        {
            registry = LocateRegistry.createRegistry( PORT );
        }
        catch ( RemoteException createException )
        {
            // Registry already exists 
            try
            {
                registry = LocateRegistry.getRegistry( PORT );
            }
            catch ( RemoteException getRegistryException )
            {
                out.println("RegistrationManager.locateRegistry: Could not get reference to existing Registry.");
                getRegistryException.printStackTrace();
                System.exit(1);
            }
            catch ( Exception getRegistryException )
            {
                out.println( getRegistryException.getMessage() );
                getRegistryException.printStackTrace();                
            }
        }
        return registry;
    }
}
