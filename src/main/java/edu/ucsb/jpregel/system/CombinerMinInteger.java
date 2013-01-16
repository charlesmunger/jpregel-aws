package edu.ucsb.jpregel.system;

import edu.ucsb.jpregel.system.Combiner;
import edu.ucsb.jpregel.system.Message;

/**
 *
 * @author Pete Cappello
 */
public class CombinerMinInteger extends Combiner<Integer, Integer>
{    
    @Override
    protected Message<Integer, Integer> combine( Message<Integer, Integer> currentMessage, Message<Integer, Integer> newMessage ) 
    {
        return ( currentMessage.getMessageValue() <= newMessage.getMessageValue() ) ? currentMessage : newMessage;
    }
}
