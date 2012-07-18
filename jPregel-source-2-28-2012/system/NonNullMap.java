package system;

import java.util.HashMap;
import java.util.TreeMap;

/**
 *
 * @author Pete Cappello
 */
final public class NonNullMap<MessageValueType> extends HashMap<Long, MessageQ<MessageValueType>>
{
    private Combiner combiner;
    
    public NonNullMap( Combiner combiner ) { this.combiner = combiner; }
        
    synchronized public MessageQ<MessageValueType> get( Long key )
    {
        MessageQ<MessageValueType> value = super.get( key );
        if ( value == null )
        {
            value = new MessageQ<MessageValueType>( combiner );
            super.put( key, value );
        }
        return value;
    }
    
    synchronized public MessageQ<MessageValueType> put( Long key, MessageQ<MessageValueType> value )
    {
        return super.put( key, value );
    }
    
    synchronized public MessageQ<MessageValueType> remove( Long key ) { return super.remove( key ); }
}
