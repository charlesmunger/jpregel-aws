package system;

import java.io.Serializable;

/**
 *
 * @author Pete Cappello
 */
public class Combiner<VertexIdType, MessageValueType> implements Serializable
{            
    protected Message<VertexIdType, MessageValueType> combine( Message<VertexIdType, MessageValueType> currentMessage, Message<VertexIdType, MessageValueType> newMessage ) { return null; } 
}
