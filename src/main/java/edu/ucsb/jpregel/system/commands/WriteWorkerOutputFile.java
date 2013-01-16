package edu.ucsb.jpregel.system.commands;

import jicosfoundation.Proxy;
import jicosfoundation.ServiceImpl;
import edu.ucsb.jpregel.system.NoFieldCommand;
import edu.ucsb.jpregel.system.Worker;

/**
 *
 * @author cappello
 */
public class WriteWorkerOutputFile extends NoFieldCommand
{

    @Override
    public void execute(Proxy proxy) { proxy.sendCommand( this ); }

    @Override
    public void execute(ServiceImpl serviceImpl) throws Exception 
    {
        Worker worker = (Worker) serviceImpl;
        worker.writeWorkerOutputFile();
    }
    
}
