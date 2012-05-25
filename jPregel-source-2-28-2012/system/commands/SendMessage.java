package system.commands;

import jicosfoundation.Command;
import jicosfoundation.Proxy;
import jicosfoundation.ServiceImpl;
import system.Message;
import system.Worker;

/**
 *
 * @author cappello
 */
public class SendMessage implements Command
{
    private Worker sendingWorker;
    private int partId;
    private int vertexId; 
    private Message message;
    private Long superStep;
    
    public SendMessage( Worker sendingWorker, int partId, int vertexId, Message message, Long superStep )
    {
        this.sendingWorker = sendingWorker;
        this.partId = partId;
        this.vertexId = vertexId;
        this.message = message;
        this.superStep = superStep;
    }

    @Override
    public void execute( Proxy proxy ) { proxy.sendCommand( this ); }

    @Override
    public void execute( ServiceImpl serviceImpl ) throws Exception 
    {
        Worker worker = (Worker) serviceImpl;
        worker.receiveMessage( sendingWorker, partId, vertexId, message, superStep );
    }
}
