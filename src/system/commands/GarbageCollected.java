package system.commands;

import jicosfoundation.Proxy;
import system.Master;
import system.NoFieldCommand;

/**
 *
 * @author Pete Cappello
 */
public class GarbageCollected extends NoFieldCommand<Master>
{
    @Override
    public void execute(Proxy proxy) { proxy.sendCommand( this ); }

    @Override
    public void execute(Master master) throws Exception { master.garbageCollected(); }   
}
