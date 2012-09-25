package system.commands;

import jicosfoundation.Command;
import jicosfoundation.Proxy;
import system.Master;

/**
 *
 * @author Pete Cappello
 */
public class GarbageCollected implements Command<Master>
{
    @Override
    public void execute(Proxy proxy) { proxy.sendCommand( this ); }

    @Override
    public void execute(Master master) throws Exception { master.garbageCollected(); }   
}
