package system;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Pete Cappello
 */
public class ActiveSet extends HashSet<Vertex> implements Factory
{
    @Override
    public Set<Vertex> make() { return Collections.synchronizedSet( new HashSet<Vertex>() ); }
    
    public boolean add( Vertex vertex ) { return super.add( vertex ); }
}
