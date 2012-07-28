package system;

import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Pete Cappello
 */
//final public class NonNullMap<VertexIdType, MessageValueType> extends HashMap<Long, MessageQ<VertexIdType, MessageValueType>>
final public class NonNullMap<VertexIdType, MessageValueType> extends ConcurrentHashMap<Long, MessageQ<VertexIdType, MessageValueType>>
{
    private Combiner combiner;
    
    public NonNullMap( Combiner combiner ) { this.combiner = combiner; }
        
//    synchronized public MessageQ<VertexIdType, MessageValueType> get( Long key )
    public MessageQ get( Long key )
    {
        putIfAbsent( key, new MessageQ<VertexIdType, MessageValueType>( combiner ) );
//        MessageQ<VertexIdType, MessageValueType> value = super.get( key );
//        if ( value == null )
//        {
//            value = new MessageQ<VertexIdType, MessageValueType>( combiner );
//            super.put( key, value );
//        }
//        return value;
        return super.get( key );
    }
    
//    synchronized public MessageQ<VertexIdType, MessageValueType> put( Long key, MessageQ<VertexIdType, MessageValueType> value )
//    public MessageQ<VertexIdType, MessageValueType> put( Long key, MessageQ<VertexIdType, MessageValueType> value )
//    {
//        return super.put( key, value );
//    }
    
//    synchronized public MessageQ<VertexIdType, MessageValueType> remove( Long key ) { return super.remove( key ); }
    public MessageQ remove( Long key ) { return super.remove( key ); }
}
