package edu.ucsb.jpregel.system.commands;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import jicosfoundation.Command;
import jicosfoundation.Proxy;
import edu.ucsb.jpregel.system.ComputeInput;
import edu.ucsb.jpregel.system.Worker;

/**
 *
 * @author Pete Cappello
 */
public class DoNextSuperStep implements Command<Worker>
{
    private ComputeInput computeInput;
    
    public DoNextSuperStep() {}
    
    public DoNextSuperStep( ComputeInput computeInput ) { this.computeInput = computeInput; }

    @Override
    public void execute( Proxy proxy ) { proxy.sendCommand( this ); }

    @Override
    public void execute( Worker worker  ) throws Exception 
    {
        worker.doNextSuperStep( computeInput );
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
