package system;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Immutable
 *
 * @author Pete Cappello
 */
public class Message<VertexIdType, ValueType> implements java.io.Externalizable
{
    private VertexIdType vertexId;
    private ValueType messageValue;
    public Message(){}
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
    
    public ValueType    getMessageValue() { return messageValue; }
    
    @Override
    public String toString()
    {
        StringBuilder string = new StringBuilder();
        string.append(vertexId).append(' ').append(messageValue);
        return new String( string );
    }

    @Override
    public void writeExternal(ObjectOutput oo) throws IOException
    {
        oo.writeObject(vertexId);
        oo.writeObject(messageValue);
    }

    @Override
    public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException
    {
        vertexId = (VertexIdType) oi.readObject();
        messageValue = (ValueType) oi.readObject();
    }
}
