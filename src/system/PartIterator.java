package system;

import static java.lang.System.out;

import java.util.Iterator;

/**
 *
 * @author Pete Cappello
 */
public class PartIterator 
{
    private Iterator<Part> partIterator;
    private int numPartsDelivered; // !! debug
        
        PartIterator( Iterator<Part> partIterator ) { this.partIterator = partIterator; 
        }
        
        synchronized Part getPart()
        {
//            if ( partIterator.hasNext() )
//            {
//                numPartsDelivered++;
//                Part part = partIterator.next();
//                if ( part == null ) out.println("      PartIterator.getPart INCORRECTLY RETURNING NULL");
//                out.println("      PartIterator.getPart: RETURNING part: " + part.getPartId() );
//                return part;
////                return partIterator.next();
//            }
//            else
//            {
//                out.println("      PartIterator.getPart: total parts delivered: " + numPartsDelivered);
//                return null;
//            }
            return partIterator.hasNext() ? partIterator.next() : null;   
        }
}
