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
 * Encapsulates a List of Commands to be sent to a particular Service.
 *
 * @version 1.0
 * @author  Peter Cappello
 */

package jicosfoundation;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;


public class Mailer extends Processor
{
    private Service fromAddress;
    private Service toAddress;   // the destination of this Mail
    private Queue<Command> commandQ = new ConcurrentLinkedQueue<Command>();
    private BlockingQueue<Mailer> mailQ; // queue of references to this.
    private RemoteExceptionHandler remoteExceptionHandler;
    private Proxy myProxy;
    private final Object commandQLock = new Object();
    
    public Mailer( Service fromAddress, RemoteExceptionHandler remoteExceptionHandler, 
                   BlockingQueue<Mailer> mailQ, Proxy myProxy) 
    { 
        super( mailQ );
             
        assert fromAddress != null;
        assert remoteExceptionHandler != null;
        
        this.fromAddress = fromAddress;
        this.remoteExceptionHandler = remoteExceptionHandler;
        this.mailQ = mailQ;
        this.myProxy = myProxy;
        toAddress = myProxy.getService();
    }
    
    /** Add a Command to the list.
     * @param command The Command object to be added.
     */    
//    public synchronized void add( Command command )
//    { 
//        assert command != null;
//        commandQ.add( command );
//        try
//        {
//            mailQ.add( this ); // notify mail processor: send commandQ
//        }
//        catch ( Exception e ) { e.printStackTrace(); }
//    }
    
    public void add( Command command )
    { 
        assert command != null;
        
        // synchronization is necessary to ensure add completes before copyCommandQ makes commandQ garbage.
        synchronized ( commandQLock ) { commandQ.add( command ); }
        try
        {
            mailQ.add( this ); // notify mail processor: send commandQ
        }
        catch ( Exception e ) { e.printStackTrace(); }
    }
    
//    private synchronized Queue copyCommandQ() 
//    {
//        Queue<Command> commandQCopy = commandQ;
//        commandQ = new ConcurrentLinkedQueue<Command>();
//        return commandQCopy;
//    }
    
    private Queue copyCommandQ() 
    {
        Queue<Command> commandQCopy = commandQ;
        commandQ = new ConcurrentLinkedQueue<Command>();
        return commandQCopy;
    }
    
    /**
     * Mail command queue to destination distributed component
     */
    @Override
    void process( Object object ) 
    {
        Queue commandQCopy;
        synchronized ( commandQLock )
        {
            if ( commandQ.isEmpty() )
            {
                return;
            }
            commandQCopy = copyCommandQ();
        }
        
        try
        {
            toAddress.receiveCommands ( fromAddress, commandQCopy );
        }
        catch ( Exception exception ) 
        {
            exception.printStackTrace();
            System.out.println( "Mail: toAddress: " + toAddress + "\n Command Q: \n");
            for ( Iterator iterator = commandQCopy.iterator(); iterator.hasNext(); )
            {
                System.out.println( ((Command) iterator.next()) );
            }
            myProxy.evict();
            System.exit( 1 ); // !! TODO modify when jPregel becomes fault-tolerant
            //remoteExceptionHandler.handle ( exception, fromAddress, toAddress );         
        }
    }
    
    /** Returns a String representation of the object.
     * @return A String representation of the object.
     */    
    public String toString() 
    {
        StringBuffer stringBuffer = new StringBuffer("Mail: toAddress: " + toAddress);
        stringBuffer.append("\n Command Q: \n");
        for ( Iterator iterator = commandQ.iterator(); iterator.hasNext(); )
        {
            stringBuffer.append( ((Command) iterator.next()).toString() );
        }
        return new String( stringBuffer );
    }
}
