package system.commands;


import jicosfoundation.Command;
import jicosfoundation.Proxy;
import jicosfoundation.Service;
import jicosfoundation.ServiceImpl;
import system.Worker;

/**
 *
 * @author Pete Cappello
 */
public class AddVertexToWorker implements Command
{
    private int    partId;
    private String vertexString;
    private Service sendingWorker;
    
    public AddVertexToWorker( int partId, String vertexString, Service sendingWorker )
    {
        this.partId = partId;
        this.vertexString = vertexString;
        this.sendingWorker = sendingWorker;
    }

    @Override
    public void execute( Proxy proxy ) { proxy.sendCommand( this ); }

    @Override
    public void execute( ServiceImpl serviceImpl ) throws Exception 
    {
        Worker worker = (Worker) serviceImpl;
        worker.addVertexToWorker( partId, vertexString, sendingWorker );
    }
}
