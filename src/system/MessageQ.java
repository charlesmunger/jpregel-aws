package system;

import java.util.LinkedList;

/**
 *
 * @author Pete Cappello
 */
final public class MessageQ<VertexIdType, MessageValueType> 
             extends    LinkedList<Message<VertexIdType, MessageValueType>>
             implements Factory
{
    private Combiner<VertexIdType, MessageValueType> combiner; // == null: Combining is disabled
        
    MessageQ( Combiner<VertexIdType, MessageValueType> combiner ) { this.combiner = combiner; } 
    
    @Override
    synchronized public boolean add( Message<VertexIdType, MessageValueType> message )
    {
        if ( ! isEmpty() && combiner != null ) 
        {
            message = combiner.combine( remove(), message );
            assert isEmpty() : size();
        }
        return super.add( message );
    }
    
    synchronized public boolean addAll( MessageQ<VertexIdType, MessageValueType> messageQ )
    {
        if ( ! isEmpty() && combiner != null ) 
        {
            assert messageQ.size() == 1 : messageQ.size();
            return super.add( combiner.combine( remove(), messageQ.remove() ) );
        }
        assert combiner == null || ( isEmpty() && messageQ.size() == 1 );
        return super.addAll( messageQ );
    }

    @Override
    public MessageQ<VertexIdType, MessageValueType> make() 
    {
        return new MessageQ<VertexIdType, MessageValueType>( combiner );
    }
}
