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
 * The Remote interface implemented by ServiceImpl.
 *
 * @version 1.0
 * @author  Peter Cappello
 */

package jicosfoundation;

import java.rmi.*;
import java.util.*;


public interface Service extends Remote
{    
    /** The name of the Service that is put in the RMIRegistry.
     */    
    public static String NAME = "Service";
    
    /** accepts a marshalled list of Commands that are being sent by another Service.
     * This method is public only because it is a Remote method: It is internal
     * to the foundation package. It distributes each Command object to its 
     * appropriate Department. Commands destined for the "ASAP_DEPARTMENT" are
     * executed directly.
     * @param sender  The sending service.
     * @param commandQ a marshalled LinkedList of Command objects.
     * @throws RemoteException This is a Remote method.
     */    
    public void receiveCommands ( Service sender, Queue<Command> commandQ) 
           throws RemoteException;
    
    /** A Remote method for synchronously executing a Command.
     * @param sender  The sending service.
     * @param command the Command object to sent for remote execution.
     * @throws RemoteException This is a Remote method.
     * @return the Object that is the return value of the Command's execute method. The actual
     * type of the object returned depends on the actual Command.
     */    
    public Object executeCommand ( Service sender, CommandSynchronous command ) 
           throws RemoteException;  
}
