package system.commands;

import jicosfoundation.Proxy;
import jicosfoundation.ServiceImpl;
import system.Master;
import system.NoFieldCommand;

/**
 *
 * @author Pete Cappello
 */
public class WorkerOutputWritten extends NoFieldCommand
{
    public WorkerOutputWritten() {}
    
    @Override
    public void execute(Proxy proxy) { proxy.sendCommand( this ); }

    @Override
    public void execute(ServiceImpl serviceImpl) throws Exception
    {
        Master master = (Master) serviceImpl;
        master.workerOutputWritten();
    }
}
