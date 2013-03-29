package edu.ucsb.jpregel.system.commands;

import jicosfoundation.Proxy;
import edu.ucsb.jpregel.system.NoFieldCommand;
import edu.ucsb.jpregel.system.Worker;

/**
 * Worker: Read & process input file.
 * @author Pete Cappello
 */
public class ReadWorkerInputFile extends NoFieldCommand<Worker>
{
    @Override
    public void execute( Proxy proxy ) { proxy.sendCommand( this ); }

    @Override
    public void execute( Worker worker ) throws Exception 
    {
        worker.processInputFile();
    }
}
