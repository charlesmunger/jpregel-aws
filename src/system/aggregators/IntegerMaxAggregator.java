package system.aggregators;

import system.Aggregator;

/**
 *
 * @author Pete Cappello
 */
public class IntegerMaxAggregator extends Aggregator<Integer>
{
    public IntegerMaxAggregator() { super(); }
    
    public IntegerMaxAggregator( Integer integer ) { super( integer ); }
    
    @Override
    public void aggregate( Aggregator<Integer> aggregator )
    { 
        if ( get() < aggregator.get() )
        {
            set( aggregator );
        }
    }
    
    @Override
    public Integer identityElement() { return new Integer( 0 ); }
    
    @Override
    public Aggregator make() { return new IntegerMaxAggregator(); }
}
