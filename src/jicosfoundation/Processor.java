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
 * Abstract Processor component
 *
 * @version 1.0
 * @author  Peter Cappello
 * 
 * Contributor(s) & tester(s): Peter Cappello 
 * 
 * Test Platform(s): 
 * Java 2 SDK, Standard Edition, Version 1.4.1_02 for Linux (Intel x86)
 */

package jicosfoundation;

import java.util.concurrent.BlockingQueue;


abstract class Processor<T> extends Thread
{
    private static int nProcessors;
    
    private BlockingQueue<T> q;            // queue of objects to be processed
    private boolean paused;             // pause processing during checkpointing        
    
    public Processor( BlockingQueue<T> q, String name ) 
    {
        super( "Jicos Processer " + (++nProcessors) + " " +name);
        assert q != null;
        
        setDaemon( true );
        this.q = q;
        start();
    }
    
    public Processor(BlockingQueue<T> q) {
        this(q,"");
    }
    abstract void process( T object );
    
    public void run()
    {                
        while ( true )
        {
//            if ( paused ) 
//            {
//                System.err.println( "Processor: pausing." );
//                try 
//                {
//                    wait();
//                } 
//                catch ( Exception ignore ) 
//                {
//                    ignore.printStackTrace();
//                }              
//                System.err.println( "Processor: resuming." );
//            }
            try
            {
                process( q.take() );
            }
            catch ( InterruptedException ignore ) {}
        }
    }   
    
//    public synchronized void setPaused( boolean paused ) 
//    { 
//        this.paused = paused; 
//        if( ! paused )
//        {
//            notify();
//        }
//    }
}
