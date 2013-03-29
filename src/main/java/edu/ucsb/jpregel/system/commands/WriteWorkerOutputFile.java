package edu.ucsb.jpregel.system.commands;

import jicosfoundation.Proxy;
import edu.ucsb.jpregel.system.NoFieldCommand;
import edu.ucsb.jpregel.system.Worker;

/**
 * Worker: Get output from your vertices & write output file.
 * @author cappello
 */
public class WriteWorkerOutputFile extends NoFieldCommand<Worker>
{
    @Override
    public void execute(Proxy proxy) { proxy.sendCommand( this ); }

    @Override
    public void execute( Worker worker ) throws Exception 
    {
        worker.writeWorkerOutputFile();
    }
}
