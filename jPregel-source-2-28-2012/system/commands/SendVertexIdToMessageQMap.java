package system.commands;

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
}
