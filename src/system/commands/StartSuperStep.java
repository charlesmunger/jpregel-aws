package system.commands;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
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
    public StartSuperStep(){}
    public StartSuperStep( ComputeInput computeInput ) { this.computeInput = computeInput; }

    @Override
    public void execute( Proxy proxy ) { proxy.sendCommand( this ); }

    @Override
    public void execute( ServiceImpl serviceImpl ) throws Exception 
    {
        Worker worker = (Worker) serviceImpl;
        worker.startSuperStep( computeInput );
    } 
    
    @Override
    public String toString()
    {
        return "StartSuperStep: ComputeInput: " + computeInput.toString();
    }

    @Override
    public void writeExternal(ObjectOutput oo) throws IOException
    {
        oo.writeObject(computeInput);
    }

    @Override
    public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException
    {
        computeInput = (ComputeInput) oi.readObject();
    }
}
