/* ************************************************************************* *
 *                                                                           *
 *        Copyright (c) 2012 Peter Cappello  <cappello@cs.ucsb.edu>          *
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
 * Administration interface.
 *
 * @author  Andy Pippin
 */

package jicosfoundation;

import java.rmi.*;


public interface Administrable extends Remote
{
    /**
     *  Perform a shutdown of this instance.
     * 
     * @return  Success (<CODE>true</CODE>), or failure (<CODE>false</CODE>).
     */
    public void  shutdown() throws RemoteException;
    
    /**
     * Get the current state of this instance.
     * 
     * @return  The current state.
     */
    //public CurrentState getCurrentState();
    
    /**
     * Get any children.
     * 
     * @return  An array of administrable instances or null, if none.
     */
    //public Administrable[]  getChildren();
    
    /**
     * Echo request ("ping");
     * 
     * @return  <CODE>request</CODE>.
     */
    public String echo( String request ) throws RemoteException;

}
