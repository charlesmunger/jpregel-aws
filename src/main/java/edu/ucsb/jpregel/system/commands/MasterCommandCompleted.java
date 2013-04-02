package edu.ucsb.jpregel.system.commands;

import edu.ucsb.jpregel.system.Master;
import edu.ucsb.jpregel.system.NoFieldCommand;
import jicosfoundation.Proxy;

/**
 * Acknowledge completion of Master command.
 * @author Pete Cappello
 */
public class MasterCommandCompleted extends NoFieldCommand<Master>
{
    public void execute( Proxy proxy ) { proxy.sendCommand( this ); }

    public void execute( Master master ) throws Exception
    {
        master.commandCompleted();
    }
}
