package system;

import java.awt.geom.Point2D;
import system.Combiner;
import system.Message;

/**
 *
 * @author Pete Cappello
 */
public class CombinerMinFloat extends Combiner<Point2D.Float, Float>
{
    @Override
    protected Message combine( Message<Point2D.Float, Float> currentMessage, Message<Point2D.Float, Float> newMessage ) 
    {
        return ( currentMessage.getMessageValue() <= newMessage.getMessageValue() ) ? currentMessage : newMessage;
    }
}
