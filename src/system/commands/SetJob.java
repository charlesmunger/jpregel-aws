package system.commands;

import jicosfoundation.Command;
import jicosfoundation.Proxy;
import jicosfoundation.ServiceImpl;
import system.Job;
import system.Worker;

/**
 *
 * @author Pete Cappello
 */
public class SetJob implements Command
{
    private Job workerJob;
    
    public SetJob( Job workerJob)
    {
        this.workerJob = workerJob;
    }

    @Override
    public void execute( Proxy proxy ) { proxy.sendCommand( this ); }

    @Override
    public void execute( ServiceImpl serviceImpl ) throws Exception 
    {
        Worker worker = (Worker) serviceImpl;
        worker.setJob( workerJob);
    }
}
