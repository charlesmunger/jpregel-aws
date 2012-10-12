package system.commands;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
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
    private int sendingWorkerNum;
    private int partId;
    private int vertexId; 
    private Message message;
    private long superStep;
    
    public SendMessage(){}
    public SendMessage( int sendingWorkerNum, int partId, int vertexId, Message message, Long superStep )
    {
        this.sendingWorkerNum = sendingWorkerNum;
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
        worker.receiveMessage( sendingWorkerNum, partId, vertexId, message, superStep );
    }

    @Override
    public void writeExternal(ObjectOutput oo) throws IOException
    {
        oo.writeInt(sendingWorkerNum);
        oo.writeInt(partId);
        oo.writeInt(vertexId);
        oo.writeObject(message);
        oo.writeLong(superStep);
    }

    @Override
    public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException
    {
        this.sendingWorkerNum = oi.readInt();
        this.partId = oi.readInt();
        this.vertexId = oi.readInt();
        this.message = (Message) oi.readObject();
        this.superStep = oi.readLong();
    }
}
