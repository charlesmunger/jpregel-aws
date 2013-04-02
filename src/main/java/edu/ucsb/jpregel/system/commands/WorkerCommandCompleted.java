package edu.ucsb.jpregel.system.commands;

import edu.ucsb.jpregel.system.NoFieldCommand;
import edu.ucsb.jpregel.system.Worker;
import jicosfoundation.Proxy;

/**
 * Acknowledge completion of Worker command.
 * @author cappello
 */
public class WorkerCommandCompleted extends NoFieldCommand<Worker>
{
    public void execute( Proxy proxy ) { proxy.sendCommand( this ); }

    public void execute( Worker worker ) throws Exception
    {
        worker.workerCommandCompleted();
    }
}
