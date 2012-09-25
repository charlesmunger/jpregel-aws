package system.commands;

import jicosfoundation.Command;
import jicosfoundation.Proxy;
import system.Worker;

/**
 *
 * @author Pete Cappello
 */
public class CollectGarbage implements Command<Worker>
{
    @Override
    public void execute(Proxy proxy) { proxy.sendCommand( this ); }

    @Override
    public void execute(Worker worker) throws Exception { worker.collectGarbage(); }   
}
