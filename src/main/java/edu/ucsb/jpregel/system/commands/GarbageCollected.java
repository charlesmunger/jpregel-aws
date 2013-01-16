package edu.ucsb.jpregel.system.commands;

import jicosfoundation.Proxy;
import edu.ucsb.jpregel.system.Master;
import edu.ucsb.jpregel.system.NoFieldCommand;

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
