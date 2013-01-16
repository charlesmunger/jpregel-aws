package edu.ucsb.jpregel.system;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import javax.activation.UnsupportedDataTypeException;

/**
 * Immutable
 *
 * @author Pete Cappello
 */
public class Message<VertexIdType, ValueType> implements java.io.Externalizable
{
    private VertexIdType vertexId;
    private ValueType messageValue;
    private static final int OBJECT = 0;
    private static final int INT = 1;
    private static final int FLOAT = 2;
    private static final int DOUBLE = 3;
    private static final int BYTE = 4;
    private static final int SHORT = 5;
    private static final int LONG = 6;
    private static final int STRING = 7;
    private static final int CHAR = 8;
    
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
        int vtype = typeOf(vertexId);
        int ttype = typeOf(messageValue);
        oo.writeByte((ttype << 4) + vtype);
        writeVar(vertexId, oo, vtype);
        writeVar(messageValue,oo,ttype);
    }

    @Override
    public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException
    {
        int type = oi.readByte();
        vertexId = (VertexIdType) readVar(oi,type & 0xF);
        messageValue = (ValueType) readVar(oi, type>> 4);
    }
    
    public static void writeVar(Object o, ObjectOutput oo,int type) throws IOException {
        switch(type) {
            case OBJECT: oo.writeObject(o); break;
            case INT: oo.writeInt((Integer) o);break;
            case FLOAT: oo.writeFloat((Float) o);break;
            case DOUBLE: oo.writeDouble((Double) o);break;
            case BYTE: oo.writeByte((Byte) o);break;
            case SHORT: oo.writeShort((Short) o);break;
            case LONG: oo.writeLong((Long) o);break;
            case STRING: oo.writeBytes((String) o);break;
            case CHAR: oo.writeChar((Character) o);break;
        }
    }
    
    public static Object readVar(ObjectInput oi,int type) throws IOException {
        switch(type) {
            case OBJECT:
                try{ return oi.readObject();} catch (ClassNotFoundException ex){
                    throw new InvalidClassException("Object read does not have a class in jpregel");
                }
            case INT:
                return oi.readInt();
            case FLOAT:
                return oi.readFloat();
            case DOUBLE:
                return oi.readDouble();
            case BYTE:
                return oi.readByte();
            case SHORT:
                return oi.readShort();
            case LONG:
                return oi.readLong();
            case STRING:
                return oi.readUTF();
            case CHAR:
                return oi.readChar();
            default:
                throw new UnsupportedDataTypeException("Unknown data type.");
        }
    }

    private static int typeOf(Object o)
    {
        if(o instanceof Number) {
            if(o instanceof Integer) {
                return INT;
            } else if(o instanceof Float) {
                return FLOAT;
            } else if(o instanceof Double) {
                return DOUBLE;
            } else if(o instanceof Byte) {
                return BYTE;
            } else if(o instanceof Long) {
                return LONG;
            } else if(o instanceof Short) {
                return SHORT;
            }
        } 
        if (o instanceof String) {
            return STRING;
        } else if (o instanceof Character) {
            return CHAR;
        }
        return OBJECT;
    }
}
