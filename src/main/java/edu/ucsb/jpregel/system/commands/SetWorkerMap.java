package edu.ucsb.jpregel.system.commands;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;
import jicosfoundation.Command;
import jicosfoundation.Proxy;
import jicosfoundation.Service;
import edu.ucsb.jpregel.system.Worker;

/**
 * Worker: Set integerToWorkerMap.
 * @author Pete Cappello
 */
public class SetWorkerMap implements Command<Worker>
{
    private Map<Integer, Service> integerToWorkerMap;
    
    public SetWorkerMap() {}
    
    public SetWorkerMap( Map<Integer, Service> integerToWorkerMap )
    { 
        this.integerToWorkerMap = integerToWorkerMap;
    }

    @Override
    public void execute(Proxy proxy) { proxy.sendCommand( this ); }

    @Override
    public void execute( Worker worker) throws Exception 
    {
        worker.setWorkerMap( integerToWorkerMap );
    }

    @Override
    public void writeExternal(ObjectOutput oo) throws IOException
    {
        oo.writeObject(integerToWorkerMap);
    }

    @Override
    public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException
    {
        integerToWorkerMap = (Map<Integer, Service>) oi.readObject();
    }
}
