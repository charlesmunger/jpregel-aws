package edu.ucsb.jpregel.system.commands;

import jicosfoundation.Proxy;
import edu.ucsb.jpregel.system.NoFieldCommand;
import edu.ucsb.jpregel.system.Worker;

/**
 *
 * @author Pete Cappello
 */
public class CollectGarbage extends NoFieldCommand<Worker>
{
    public CollectGarbage() {}
    
    @Override
    public void execute(Proxy proxy) { proxy.sendCommand( this ); }

    @Override
    public void execute(Worker worker) throws Exception { worker.collectGarbage(); }   
}
