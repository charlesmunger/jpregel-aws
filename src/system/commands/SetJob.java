package system.commands;

import jicosfoundation.Command;
import jicosfoundation.Proxy;
import jicosfoundation.ServiceImpl;
import system.Worker;
import system.Job;

/**
 *
 * @author Pete Cappello
 */
public class SetJob implements Command
{
    private Job workerJob;
    private boolean isEc2;
    
    public SetJob( Job workerJob, boolean isEc2 )
    {
        this.workerJob = workerJob;
        this.isEc2 = isEc2;
    }

    @Override
    public void execute( Proxy proxy ) { proxy.sendCommand( this ); }

    @Override
    public void execute( ServiceImpl serviceImpl ) throws Exception 
    {
        Worker worker = (Worker) serviceImpl;
        worker.setJob( workerJob, isEc2 );
    }
}
