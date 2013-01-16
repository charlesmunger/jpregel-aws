package edu.ucsb.jpregel.system;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author Pete Cappello
 */
final public class MessageQ<VertexIdType, MessageValueType> 
             extends    ConcurrentLinkedQueue<Message<VertexIdType, MessageValueType>>
             implements Factory
{
    private Combiner<VertexIdType, MessageValueType> combiner; // == null: Combining is disabled
        
    MessageQ( Combiner<VertexIdType, MessageValueType> combiner ) { this.combiner = combiner; } 
    
    @Override
    public boolean add( Message<VertexIdType, MessageValueType> message )
    {
        if ( combiner != null ) 
        {
            Message polled = poll();
            if ( polled != null )
            {
                assert size() == 1 : size();
                message = combiner.combine( polled, message ); 
            }
        }
        return super.add( message );
    }
    
    public boolean addAll( MessageQ<VertexIdType, MessageValueType> messageQ )
    {
        if ( combiner != null ) 
        {
            Message polled = poll();
            if ( polled != null )
            {
                assert messageQ.size() == 1 : messageQ.size();
                return super.add( combiner.combine( polled, messageQ.remove() ) );
            }
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
