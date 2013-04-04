package edu.ucsb.jpregel.system.commands;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import jicosfoundation.Command;
import jicosfoundation.Proxy;
import edu.ucsb.jpregel.system.Job;
import edu.ucsb.jpregel.system.Worker;

/**
 *
 * @author Pete Cappello
 */
public class SetJob implements Command<Worker>
{
    private Job workerJob;
    
    public SetJob() {}
    
    public SetJob( Job workerJob)
    {
        this.workerJob = workerJob;
    }

    @Override
    public void execute( Proxy proxy ) { proxy.sendCommand( this ); }

    @Override
    public void execute( Worker worker ) throws Exception 
    {
        worker.setJob( workerJob);
    }

    @Override
    public void writeExternal(ObjectOutput oo) throws IOException
    {
        oo.writeObject(workerJob);
    }

    @Override
    public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException
    {
        workerJob = (Job) oi.readObject();
    }
}
