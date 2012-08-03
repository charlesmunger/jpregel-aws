package system.commands;

import jicosfoundation.Command;
import jicosfoundation.Proxy;
import jicosfoundation.ServiceImpl;
import system.ComputeInput;
import system.Worker;

/**
 *
 * @author Pete Cappello
 */
public class ShutdownWorker implements Command
{    
    @Override
    public void execute( Proxy proxy ) { proxy.sendCommand( this ); }

    @Override
    public void execute( ServiceImpl serviceImpl ) throws Exception 
    {
        Worker worker = (Worker) serviceImpl;
        worker.shutdown();
    } 
    
    public String toString() { return this.getClass().getName(); } 
}
