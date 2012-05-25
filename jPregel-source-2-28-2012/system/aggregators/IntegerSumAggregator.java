package system.aggregators;

import system.Aggregator;

/**
 *
 * @author Pete Cappello
 */
public class IntegerSumAggregator extends Aggregator<Integer>
{
    public IntegerSumAggregator() { super(); }
    
    public IntegerSumAggregator( Integer integer ) { super( integer ); }
    
    @Override
    public void aggregate( Aggregator<Integer> aggregator ) { this.element += aggregator.get(); }
    
    @Override
    public Integer identityElement() { return new Integer( 0 ); }
    
    @Override
    public Aggregator make() { return new IntegerSumAggregator(); }
    
    public String toString() { return get().toString(); }
}
