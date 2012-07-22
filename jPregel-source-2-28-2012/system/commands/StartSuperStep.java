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
public class StartSuperStep implements Command
{
    private ComputeInput computeInput;
    
    public StartSuperStep( ComputeInput computeInput ) { this.computeInput = computeInput; }

    @Override
    public void execute( Proxy proxy ) { proxy.sendCommand( this ); }

    @Override
    public void execute( ServiceImpl serviceImpl ) throws Exception 
    {
        Worker worker = (Worker) serviceImpl;
        worker.startSuperStep( computeInput );
    } 
    
    public String toString()
    {
        return "StartSuperStep: ComputeInput: " + computeInput.toString();
    }
}
