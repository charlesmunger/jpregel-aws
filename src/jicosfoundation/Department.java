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
 *  A Service partitions the set of Command types. Each part is associated with
 *  a Department, which has a set of CommandProcessor objects (threads), each of 
 *  which removes/processes Command objects from the Department's Command queue.
 *
 * @author Peter Cappello
 * @version 1
 */

package jicosfoundation;

import java.util.*;
import java.util.concurrent.BlockingQueue;


public final class Department 
{
    private BlockingQueue q;
    private Vector commandProcessors = new Vector();
    private ServiceImpl myService;
    
    /** Construct a Department.
     * @param myService A reference to the Service that is constructing this 
     * Department.
     * @param $Processors The number of CommandProcessor objects to associate 
     * with this Department.
     */    
    public Department ( ServiceImpl myService, BlockingQueue q, int $Processors ) 
    {
        assert myService != null;
        assert q != null;
        
        this.myService = myService;
        this.q = q;
        for ( int i = 0; i < $Processors; i++ )
        {
            addProcessor();
        }       
    }
    
    /** Add a Command object to the Department's Command queue.
     * @param command The Command object to be added to the Command queue.
     */    
    public void addCommand ( Command command )
    { 
        assert command != null;       
        q.add( command ); 
    }
    
    public void addProcessor()
    {
        commandProcessors.add ( new CommandProcessor ( q, myService ) );
    }
    
    public void addProcessors( int $processors )
    {
        assert $processors >= 0;       
        for ( int i = 0; i < $processors; i++ )
        {
            addProcessor();
        }
    }
    
    public void removeProcessor()
    {
        assert commandProcessors.size() > 0;        
        commandProcessors.removeElementAt( commandProcessors.size() - 1 );
    }
    
    public BlockingQueue q() { return q; }
    
//    public void setPaused( boolean pause )
//    {
//        System.err.println( "Department::setPaused:" + pause );
//        for ( int i = 0; i < commandProcessors.size(); i++ )
//        {
//            System.err.println( "Department::setPaused:processor:" + i );
//            ((Processor) commandProcessors.get( i )).setPaused( pause );            
//        }
//    }
}
