package system.commands;

import jicosfoundation.Proxy;
import system.NoFieldCommand;
import system.Worker;

/**
 *
 * @author Pete Cappello
 */
public class CollectGarbage extends NoFieldCommand<Worker>
{
    @Override
    public void execute(Proxy proxy) { proxy.sendCommand( this ); }

    @Override
    public void execute(Worker worker) throws Exception { worker.collectGarbage(); }   
}
