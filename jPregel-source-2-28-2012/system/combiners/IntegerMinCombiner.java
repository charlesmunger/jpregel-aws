package system.combiners;

import system.Combiner;
import system.Message;

/**
 *
 * @author Pete Cappello
 */
public class IntegerMinCombiner extends Combiner<Integer, Integer>
{    
    protected Message<Integer, Integer> combine( Message<Integer, Integer> currentMessage, Message<Integer, Integer> newMessage ) 
    {
        return ( currentMessage.getMessageValue() <= newMessage.getMessageValue() ) ? currentMessage : newMessage;
    }
}
