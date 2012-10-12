package system.commands;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import jicosfoundation.Command;
import jicosfoundation.Proxy;
import jicosfoundation.ServiceImpl;
import system.Worker;
/**
 *
 * @author Pete Cappello
 */
public class AddVertexToPartComplete implements Command
{

    @Override
    public void execute( Proxy proxy ) { proxy.sendCommand( this ); }

    @Override
    public void execute(ServiceImpl serviceImpl) throws Exception 
    {
        Worker worker = (Worker) serviceImpl;
        worker.addVertexToPartComplete();
    }

    @Override
    public void writeExternal(ObjectOutput oo) throws IOException{}

    @Override
    public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException{}
}
