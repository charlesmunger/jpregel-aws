package system.commands;

import static java.lang.System.out;

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
}
