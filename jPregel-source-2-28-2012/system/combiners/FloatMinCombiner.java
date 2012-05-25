package system.combiners;

import system.Combiner;
import system.Message;

/**
 *
 * @author Pete Cappello
 */
public class FloatMinCombiner extends Combiner<Float>
{
    protected Message<Float> combine( Message<Float> currentMessage, Message<Float> newMessage ) 
    {
        return ( currentMessage.getMessageValue() <= newMessage.getMessageValue() ) ? currentMessage : newMessage;
    }
}
