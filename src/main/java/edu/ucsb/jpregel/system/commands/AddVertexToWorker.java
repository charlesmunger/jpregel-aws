package edu.ucsb.jpregel.system.commands;


import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import jicosfoundation.Command;
import jicosfoundation.Proxy;
import jicosfoundation.ServiceImpl;
import edu.ucsb.jpregel.system.Worker;

/**
 *
 * @author Pete Cappello
 */
public class AddVertexToWorker implements Command
{
    private int    partId;
    private String vertexString;
    private int sendingWorkerNum;
    
    public AddVertexToWorker(){}
    public AddVertexToWorker( int partId, String vertexString, int sendingWorkerNum )
    {
        this.partId = partId;
        this.vertexString = vertexString;
        this.sendingWorkerNum = sendingWorkerNum;
    }

    @Override
    public void execute( Proxy proxy ) { proxy.sendCommand( this ); }

    @Override
    public void execute( ServiceImpl serviceImpl ) throws Exception 
    {
        Worker worker = (Worker) serviceImpl;
        worker.addVertexToWorker( partId, vertexString, sendingWorkerNum );
    }

    @Override
    public void writeExternal(ObjectOutput oo) throws IOException
    {
        oo.writeInt(partId);
        oo.writeUTF(vertexString);
        oo.writeInt(sendingWorkerNum);
    }

    @Override
    public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException
    {
        partId = oi.readInt();
        vertexString = oi.readUTF();
        sendingWorkerNum = oi.readInt();
    }
}
