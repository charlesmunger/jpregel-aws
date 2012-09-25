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
 * Defines a map: Command |--> int
 *
 * @author  Peter Cappello
 */

package jicosfoundation;

import java.util.HashMap;


public final class Class2Int extends HashMap 
{
    public Class2Int( Class[][] class2Int ) 
    {        
        for ( int i = 0; i < class2Int.length;    i++ ) 
        {
            Integer level = new Integer( i );
            for ( int j = 0; j < class2Int[i].length; j++ ) 
            {
                Class className = class2Int[i][j];
                put ( className, level );
            }
        }
    }
    
    public int map( Object object ) //throws Exception 
    { 
        Class className = object.getClass();
        Integer level = (Integer) get( className );
        if ( level == null )
        {
            throw new IllegalArgumentException("Unmapped Class " + className);
        }
        return level.intValue();
    }
}
