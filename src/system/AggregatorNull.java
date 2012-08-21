package system;

import api.Aggregator;

/**
 *
 * @author Pete Cappello
 */
public class AggregatorNull extends Aggregator
{
    @Override
    public void aggregate( Aggregator ignore ) {}

    @Override
    public Object identityElement() { return null; }

    @Override
    public Aggregator make() { return new AggregatorNull(); }
}
