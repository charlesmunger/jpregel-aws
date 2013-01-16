package edu.ucsb.jpregel.system.commands;

import jicosfoundation.Proxy;
import jicosfoundation.ServiceImpl;
import edu.ucsb.jpregel.system.Master;
import edu.ucsb.jpregel.system.NoFieldCommand;

/**
 *
 * @author cappello
 */
public class WorkerMapSet extends NoFieldCommand
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
