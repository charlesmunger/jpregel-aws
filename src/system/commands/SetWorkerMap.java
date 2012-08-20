package system.commands;

import java.util.Map;
import jicosfoundation.Command;
import jicosfoundation.Proxy;
import jicosfoundation.Service;
import jicosfoundation.ServiceImpl;
import system.Worker;

/**
 *
 * @author Pete Cappello
 */
public class SetWorkerMap implements Command
{
    private Map<Integer, Service> integerToWorkerMap;
    
    public SetWorkerMap( Map<Integer, Service> integerToWorkerMap )
    { 
        this.integerToWorkerMap = integerToWorkerMap;
    }

    @Override
    public void execute(Proxy proxy) { proxy.sendCommand( this ); }

    @Override
    public void execute(ServiceImpl serviceImpl) throws Exception 
    {
        Worker worker = (Worker) serviceImpl;
        worker.setWorkerMap( integerToWorkerMap );
    }
}
