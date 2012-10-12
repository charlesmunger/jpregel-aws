package system.commands;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import jicosfoundation.Command;
import jicosfoundation.Proxy;
import jicosfoundation.ServiceImpl;
import system.ComputeOutput;
import system.Master;

/**
 *
 * @author Pete Cappello
 */
public class SuperStepComplete implements Command
{
    ComputeOutput computeOutput;
    public SuperStepComplete(){}
    public SuperStepComplete( ComputeOutput computeOutput ) { this.computeOutput = computeOutput; }

    @Override
    public void execute(Proxy proxy) 
    { 
        proxy.sendCommand( this ); 
    }

    @Override
    public void execute(ServiceImpl serviceImpl) throws Exception 
    {
        Master master = (Master) serviceImpl;
        master.superStepComplete( computeOutput );
    }

    @Override
    public void writeExternal(ObjectOutput oo) throws IOException
    {
        oo.writeObject(computeOutput);
    }

    @Override
    public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException
    {
        computeOutput = (ComputeOutput) oi.readObject();
    }
}
