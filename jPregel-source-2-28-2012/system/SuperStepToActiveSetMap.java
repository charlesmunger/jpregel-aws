package system;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Pete Cappello
 */
public class SuperStepToActiveSetMap extends HashMap<Long,Set<Vertex>>
{
    synchronized public Set<Vertex> get( Long superStep )
    {
        Set<Vertex> activeSet = super.get( superStep );
        if ( activeSet == null )
        {
            activeSet = new HashSet<Vertex>();
            super.put( superStep, activeSet );
        }
        return activeSet;
    }
    
    synchronized public Set<Vertex> put( Long superStep, Set<Vertex> activeSet )
    {
        return super.put( superStep, activeSet );
    }
    
    synchronized public Set<Vertex> remove( Long superStep ) { return super.remove( superStep ); }
}
