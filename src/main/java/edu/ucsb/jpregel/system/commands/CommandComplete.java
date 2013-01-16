package edu.ucsb.jpregel.system.commands;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import jicosfoundation.Command;
import jicosfoundation.Proxy;
import jicosfoundation.ServiceImpl;
import edu.ucsb.jpregel.system.Master;

/**
 *
 * @author Pete Cappello
 */
public class CommandComplete implements Command
{

    private int workerNum;

    public CommandComplete(){}
    
    public CommandComplete(int workerNum)
    {
        this.workerNum = workerNum;
    }

    @Override
    public void execute(Proxy proxy)
    {
        proxy.sendCommand(this);
    }

    @Override
    public void execute(ServiceImpl serviceImpl) throws Exception
    {
        Master master = (Master) serviceImpl;
        master.commandComplete(workerNum);
    }

    @Override
    public void writeExternal(ObjectOutput oo) throws IOException
    {
        oo.write(workerNum);
    }

    @Override
    public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException
    {
        workerNum = oi.readInt();
    }
}
