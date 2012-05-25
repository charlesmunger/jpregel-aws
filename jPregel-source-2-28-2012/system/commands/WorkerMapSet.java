package system.commands;

import jicosfoundation.Command;
import jicosfoundation.Proxy;
import jicosfoundation.ServiceImpl;
import system.Master;

/**
 *
 * @author cappello
 */
public class WorkerMapSet implements Command
{    
    public WorkerMapSet() {}

    @Override
    public void execute(Proxy proxy) { proxy.sendCommand( this ); }

    @Override
    public void execute(ServiceImpl serviceImpl) throws Exception 
    {
        Master master = (Master) serviceImpl;
        master.workerMapSet();
    }
}
