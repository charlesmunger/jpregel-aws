package system.combiners;

import java.awt.geom.Point2D;
import system.Combiner;
import system.Message;

/**
 *
 * @author Pete Cappello
 */
public class FloatMinCombiner extends Combiner<Point2D.Float, Float>
{
    protected Message combine( Message<Point2D.Float, Float> currentMessage, Message<Point2D.Float, Float> newMessage ) 
    {
        return ( currentMessage.getMessageValue() <= newMessage.getMessageValue() ) ? currentMessage : newMessage;
    }
}
