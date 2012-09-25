package system.commands;

import jicosfoundation.Command;
import jicosfoundation.Proxy;
import jicosfoundation.ServiceImpl;
import system.Master;

/**
 *
 * @author Pete Cappello
 */
public class WorkerOutputWritten implements Command
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
