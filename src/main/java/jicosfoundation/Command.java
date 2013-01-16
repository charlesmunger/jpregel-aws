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
 * All commands objects that are passed between distributed
 * services implemented with the edu.ucsb.cs.jicos.foundation package
 * must implement Command.
 *
 * @version 1
 * @author  Peter Cappello
 */

package jicosfoundation;

public interface Command<S extends ServiceImpl> extends java.io.Externalizable
{    
    /** Proxy invokes this method.
     * 1. update local state, if necessary.
     * 2. process command locally, to the extent possible.
     * 3. proxy.sendCommand( this ) execute by actual Service, if needed.
     */
    void execute( Proxy proxy ); //throws Exception;
    
    /** Remote server executes this method. Implementation is empty, when 
     * Command is implemented locally (i.e., entirely by Proxy).
     */
    void execute( S serviceImpl ) throws Exception;        
}
