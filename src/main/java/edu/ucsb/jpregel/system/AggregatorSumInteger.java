package edu.ucsb.jpregel.system;

import api.Aggregator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Aggregates sum of AtomicInteger objects.
 * Thread-safe.
 * @author Pete Cappello
 */
public class AggregatorSumInteger extends Aggregator<AtomicInteger>
{
    private static final AtomicInteger ZERO = new AtomicInteger();
    
    public AggregatorSumInteger() { super(); }
    
    public AggregatorSumInteger( AtomicInteger atomicInteger ) { super( atomicInteger ); }
    
    @Override
    public void aggregate( Aggregator<AtomicInteger> aggregator )
    {
        // 1st get returns AtomicInteger; 2nd get returns its int value
        element.addAndGet( aggregator.get().get() );
    }
    
    @Override
    public AtomicInteger identityElement() { return ZERO; }
    
    @Override
    public Aggregator make() { return new AggregatorSumInteger(); }
    
    @Override
    public String toString() { return get().toString(); }
}
