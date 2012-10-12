package system.commands;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;
import jicosfoundation.Command;
import jicosfoundation.Proxy;
import jicosfoundation.Service;
import jicosfoundation.ServiceImpl;
import system.MessageQ;
import system.Worker;

/**
 *
 * @author Pete Cappello
 */
public class SendVertexIdToMessageQMap implements Command
{
    private Service sendingWorker;
    private Map<Object, MessageQ> vertexIdToMessageQMap;
    private Long superStep;
    
    public SendVertexIdToMessageQMap(){}
    public SendVertexIdToMessageQMap( Service sendingWorker, Map<Object, MessageQ> vertexIdToMessageQMap, Long superStep )
    {
        this.sendingWorker = sendingWorker;
        this.vertexIdToMessageQMap = vertexIdToMessageQMap;
        this.superStep = superStep;
    }

    @Override
    public void execute( Proxy proxy ) { proxy.sendCommand( this ); }

    @Override
    public void execute(ServiceImpl serviceImpl) throws Exception 
    {
        Worker worker = (Worker) serviceImpl;
        worker.receiveVertexIdToMessageQMap( sendingWorker, vertexIdToMessageQMap, superStep );
    }

    @Override
    public void writeExternal(ObjectOutput oo) throws IOException
    {
        oo.writeObject(sendingWorker);
        oo.writeObject(vertexIdToMessageQMap);
        oo.writeObject(superStep);
    }

    @Override
    public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException
    {
        sendingWorker = (Service) oi.readObject();
        vertexIdToMessageQMap = (Map<Object, MessageQ>) oi.readObject();
        superStep = (Long) oi.readObject();
    }
}
