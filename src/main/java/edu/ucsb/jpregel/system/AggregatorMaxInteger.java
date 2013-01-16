package edu.ucsb.jpregel.system;

import api.Aggregator;

/**
 *
 * @author Pete Cappello
 */
public class AggregatorMaxInteger extends Aggregator<Integer>
{
    public AggregatorMaxInteger() { super(); }
    
    public AggregatorMaxInteger( Integer integer ) { super( integer ); }
    
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
    public Aggregator make() { return new AggregatorMaxInteger(); }
}
