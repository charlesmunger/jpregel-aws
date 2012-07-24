package system.combiners;

import system.Combiner;
import system.Message;

/**
 *
 * @author Pete Cappello
 */
public class IntegerMinCombiner extends Combiner<Integer>
{    
    protected Message<?, Integer> combine( Message<?, Integer> currentMessage, Message<?, Integer> newMessage ) 
    {
        return ( currentMessage.getMessageValue() <= newMessage.getMessageValue() ) ? currentMessage : newMessage;
    }
}
