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
 * This Command type is used to pass a list of Command objects as a single
 * Command object. The contained Command objects can themselves be of type
 * CommandCollection, thus enabling a single Command object to encapsulate
 * a tree of Command objects.
 *
 * The alternative is to have separate mail methods for add and send. However,
 * the standard case is to send immediately after add. The current design 
 * respects this case: The Mail add method implicitly sends the Mail.
 * CommandList allows the Service to build up several Command objects before 
 * sending them (the non-standard case).
 *
 * @author  Peter Cappello
 */

package jicosfoundation;

import java.util.*;


public final class CommandList implements Command 
{
    private Queue q = new LinkedList();
    
    /** add a Command object to the list.
     * @param command The Command object to be added to the list.
     */    
    public final void add ( Command command ) 
    { 
        // pre-condition
        assert command != null;
        
        q.add ( command ); 
    }
    
    final Queue q() { return q; }
    
    public void execute( Proxy proxy ) //throws Exception 
    { proxy.sendCommand( this ); }
    
    /** For this Command subclass, the execute method is unused.
     * @param myService Unused in this class.
     *
     * !! Check to see if this makes sense when using proxies.
     */    
    public void execute(ServiceImpl myService) {}
}
