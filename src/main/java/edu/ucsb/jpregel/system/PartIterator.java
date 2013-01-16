package edu.ucsb.jpregel.system;


import java.util.Iterator;

/**
 *
 * @author Pete Cappello
 */
public class PartIterator 
{
    private Iterator<Part> partIterator;
        
        PartIterator( Iterator<Part> partIterator ) { this.partIterator = partIterator; 
        }
        
        synchronized Part getPart()
        {
            return partIterator.hasNext() ? partIterator.next() : null;   
        }
}
