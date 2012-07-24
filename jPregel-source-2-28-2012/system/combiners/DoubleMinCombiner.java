package system.combiners;

import system.Combiner;
import system.Message;

/**
 *
 * @author Pete Cappello
 */
public class DoubleMinCombiner extends Combiner<Double>
{    
    protected Message<?, Double> combine( Message<?, Double> currentMessage, Message<?, Double> newMessage ) 
    {
        return ( currentMessage.getMessageValue() <= newMessage.getMessageValue() ) ? currentMessage : newMessage;
    }
}
