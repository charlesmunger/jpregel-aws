package system;

/**
 * Immutable
 *
 * @author Pete Cappello
 */
public class Message<VertexIdType, ValueType> implements java.io.Serializable
{
    private VertexIdType vertexId;
    private ValueType messageValue;
    
    public Message( VertexIdType vertexId, ValueType messageValue )
    {
        this.vertexId     = vertexId;
        this.messageValue = messageValue;
    }
    
    public Message( Message<VertexIdType, ValueType> sourceMessage )
    {
        vertexId     = sourceMessage.getVertexId();
        messageValue = sourceMessage.getMessageValue();
    }
    
    public VertexIdType getVertexId()     { return vertexId; }
    
    public ValueType getMessageValue() { return messageValue; }
    
    @Override
    public String toString()
    {
        StringBuilder string = new StringBuilder();
        string.append(vertexId).append(' ').append(messageValue);
        return new String( string );
    }
}
