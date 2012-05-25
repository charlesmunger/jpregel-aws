package system;

/**
 * Immutable
 *
 * @author Pete Cappello
 */
public class Message<ValueType> implements java.io.Serializable
{
    private Object vertexId;
    private ValueType messageValue;
    
    public Message( Object vertexId, ValueType messageValue )
    {
        this.vertexId     = vertexId;
        this.messageValue = messageValue;
    }
    
    public Message( Message<ValueType> sourceMessage )
    {
        vertexId     = sourceMessage.getVertexId();
        messageValue = sourceMessage.getMessageValue();
    }
    
    public Object getVertexId()     { return vertexId; }
    
    public ValueType getMessageValue() { return messageValue; }
}
