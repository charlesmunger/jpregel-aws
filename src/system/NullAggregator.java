package system;

/**
 *
 * @author Pete Cappello
 */
public class NullAggregator extends Aggregator
{
    @Override
    public void aggregate( Aggregator ignore ) {}

    @Override
    public Object identityElement() { return null; }

    @Override
    public Aggregator make() { return new NullAggregator(); }
}
