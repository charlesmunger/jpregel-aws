package system;

import api.Aggregator;

/**
 *
 * @author Pete Cappello
 */
public class AggregatorSumInteger extends Aggregator<Integer>
{
    public AggregatorSumInteger() { super(); }
    
    public AggregatorSumInteger( Integer integer ) { super( integer ); }
    
    @Override
    public void aggregate( Aggregator<Integer> aggregator ) { this.element += aggregator.get(); }
    
    @Override
    public Integer identityElement() { return new Integer( 0 ); }
    
    @Override
    public Aggregator make() { return new AggregatorSumInteger(); }
    
    @Override
    public String toString() { return get().toString(); }
}
