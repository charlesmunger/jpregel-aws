package system;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Pete Cappello
 */
public class SuperStepToActiveSetMap extends ConcurrentHashMap<Long,Set<Vertex>>
{
//    synchronized public Set<Vertex> get( long superStep )
    public Set<Vertex> get( long superStep )
    {
        putIfAbsent( superStep, new HashSet<Vertex>() );
//        Set<Vertex> activeSet = super.get( superStep );
//        if ( activeSet == null )
//        {
//            activeSet = new HashSet<Vertex>();
//            super.put( superStep, activeSet );
//        }
//        return activeSet;
        return super.get( superStep );
    }
    
//    synchronized public Set<Vertex> put( Long superStep, Set<Vertex> activeSet )
    public Set<Vertex> put( Long superStep, Set<Vertex> activeSet )
    {
        return super.put( superStep, activeSet );
    }
    
//    synchronized public Set<Vertex> remove( Long superStep ) { return super.remove( superStep ); }
    public Set<Vertex> remove( Long superStep ) { return super.remove( superStep ); }
}
