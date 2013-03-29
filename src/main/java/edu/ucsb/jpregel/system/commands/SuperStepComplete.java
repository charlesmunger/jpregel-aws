package edu.ucsb.jpregel.system.commands;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import jicosfoundation.Command;
import jicosfoundation.Proxy;
import jicosfoundation.ServiceImpl;
import edu.ucsb.jpregel.system.ComputeOutput;
import edu.ucsb.jpregel.system.Master;

/**
 *
 * @author Pete Cappello
 */
public class SuperStepComplete implements Command<Master>
{
    private ComputeOutput computeOutput;

    public SuperStepComplete( ComputeOutput computeOutput ) { this.computeOutput = computeOutput; }

    @Override
    public void execute(Proxy proxy) { proxy.sendCommand( this ); }

    @Override
    public void execute( Master master ) throws Exception 
    {
        master.superStepComplete( computeOutput );
    }

    @Override
    public void writeExternal( ObjectOutput oo ) throws IOException
    {
        oo.writeObject( computeOutput );
    }

    @Override
    public void readExternal( ObjectInput oi ) throws IOException, ClassNotFoundException
    {
        computeOutput = ( ComputeOutput ) oi.readObject();
    }
}
