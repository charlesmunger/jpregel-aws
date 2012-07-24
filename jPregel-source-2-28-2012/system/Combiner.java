package system;

import java.io.Serializable;

/**
 *
 * @author Pete Cappello
 */
public class Combiner<MessageValueType> implements Serializable
{            
    protected Message<?, MessageValueType> combine( Message<?, MessageValueType> currentMessage, Message<?, MessageValueType> newMessage ) { return null; } 
}
