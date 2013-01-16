package edu.ucsb.jpregel.system;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Pete Cappello
 */
public class ActiveSet extends HashSet<VertexImpl> implements Factory
{
    @Override
    public Set<VertexImpl> make() { return Collections.synchronizedSet( new HashSet<VertexImpl>() ); }
    
    @Override
    public boolean add( VertexImpl vertex ) { return super.add( vertex ); }
}
