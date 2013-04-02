package edu.ucsb.jpregel.system;

import api.Aggregator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Aggregates maximum value of AtomicInteger objects.
 * Thread-safe.
 * @author Pete Cappello
 */
public class AggregatorMaxInteger extends Aggregator<AtomicInteger>
{
    public AggregatorMaxInteger() { super(); }
    
    public AggregatorMaxInteger( AtomicInteger atomicInteger ) { super( atomicInteger ); }
    
    @Override
    synchronized public void aggregate( Aggregator<AtomicInteger> aggregator )
    {
        // 1st get returns AtomicInteger; 2nd returns int value
        if ( get().get() < aggregator.get().get() )
        {
            set( aggregator );
        }
    }
    
    @Override
    public AtomicInteger identityElement() { return new AtomicInteger(); }
    
    @Override
    public Aggregator make() { return new AggregatorMaxInteger(); }
}
