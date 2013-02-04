package edu.ucsb.jpregel.system;

import api.Aggregator;

/**
 *
 * @author Pete Cappello
 */
public class AggregatorSumInteger extends Aggregator<Integer>
{
    private static final Integer ZERO = new Integer( 0 );
    public AggregatorSumInteger() { super(); }
    
    public AggregatorSumInteger( Integer integer ) { super( integer ); }
    
    @Override
    public void aggregate( Aggregator<Integer> aggregator ) { this.element += aggregator.get(); }
    
    @Override
    public Integer identityElement() { return ZERO; }
    
    @Override
    public Aggregator make() { return new AggregatorSumInteger(); }
    
    @Override
    public String toString() { return get().toString(); }
}
