package edu.ucsb.jpregel.system.commands;

import jicosfoundation.Proxy;
import jicosfoundation.ServiceImpl;
import edu.ucsb.jpregel.system.NoFieldCommand;
import edu.ucsb.jpregel.system.Worker;

/**
 *
 * @author Pete Cappello
 */
public class ShutdownWorker extends NoFieldCommand
{    
    @Override
    public void execute( Proxy proxy ) { proxy.sendCommand( this ); }

    @Override
    public void execute( ServiceImpl serviceImpl ) throws Exception 
    {
        Worker worker = (Worker) serviceImpl;
        worker.shutdown();
    } 
    
    @Override
    public String toString() { return this.getClass().getName(); } 
}
