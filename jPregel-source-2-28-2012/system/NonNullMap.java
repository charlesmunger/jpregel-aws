package system;

import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Pete Cappello
 */
final public class NonNullMap<VertexIdType, MessageValueType> extends ConcurrentHashMap<Long, MessageQ<VertexIdType, MessageValueType>>
{
    private Combiner combiner;
    
    public NonNullMap( Combiner combiner ) { this.combiner = combiner; }
        
    public MessageQ get( Long key )
    {
        putIfAbsent( key, new MessageQ<VertexIdType, MessageValueType>( combiner ) );
        return super.get( key );
    }
    
    public MessageQ remove( Long key ) { return super.remove( key ); }
}
