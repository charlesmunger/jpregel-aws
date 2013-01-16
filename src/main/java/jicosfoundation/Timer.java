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
/*
 * Timer.java
 *
 * I would prefer for this class to extend Thread. But, I want to ensure that
 * operations on it are synchronized. Wrapping the TreeMap in a synchronizer,
 * such as
 *
 *  Map map = Collections.synchronizedMap( new TreeMap() );
 *
 * does not work, since map then does not have a firstKey() method.
 * Consequently, the class does its own syncrhonization.
 */

package jicosfoundation;

import java.util.*;

/**
 *
 * @author Pete Cappello
 */
final class Timer extends TreeMap implements Runnable
{
    Timer() { new Thread( this ).start(); }
    
    public void run()
    {
        while ( true )
        {
             while ( isEmpty() )
            {                                 
                try 
                {
                    wait(); 
                } 
                catch ( InterruptedException ignore ){}                     
            }
            assert ! isEmpty();            
            Proxy proxy = (Proxy) remove( firstKey() );
        }
    }
}
