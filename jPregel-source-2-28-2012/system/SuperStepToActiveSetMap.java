package system;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Pete Cappello
 */
public class SuperStepToActiveSetMap extends ConcurrentHashMap<Long, Set<Vertex>>
{
    public Set get( long superStep )
    {
        putIfAbsent( superStep, Collections.synchronizedSet( new HashSet<Vertex>() ) );
        return super.get( superStep );
    }
    
    public Set remove( Long superStep ) { return super.remove( superStep ); }
}
