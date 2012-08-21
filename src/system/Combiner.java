package system;

import java.io.Serializable;

/**
 *
 * @author Pete Cappello
 */
public class Combiner<VertexIdType, ValueType> implements Serializable
{
    // TODO Combiner: Why does combine method produce Javadoc errors?
    protected Message<VertexIdType, ValueType> combine( Message<VertexIdType, ValueType> currentMessage, Message<VertexIdType, ValueType> newMessage ) { return null; } 
}
