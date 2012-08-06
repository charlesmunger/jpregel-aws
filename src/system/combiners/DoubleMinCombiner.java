package system.combiners;

import java.awt.geom.Point2D;
import system.Combiner;
import system.Message;

/**
 *
 * @author Pete Cappello
 */
public class DoubleMinCombiner extends Combiner<Point2D.Float, Double>
{    
    @Override
    protected Message<Point2D.Float, Double> combine( Message<Point2D.Float, Double> currentMessage, Message<Point2D.Float, Double> newMessage ) 
    {
        return ( currentMessage.getMessageValue() <= newMessage.getMessageValue() ) ? currentMessage : newMessage;
    }
}
