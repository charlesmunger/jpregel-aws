package system.commands;

import static java.lang.System.out;

import jicosfoundation.Command;
import jicosfoundation.Proxy;
import jicosfoundation.Service;
import jicosfoundation.ServiceImpl;
import system.Vertex;
import system.Worker;
import system.ProxyWorker;

/**
 *
 * @author Pete Cappello
 */
public class AddVertexToWorker implements Command
{
    private int    partId;
//    private Vertex vertex;
    private String vertexString;
    private Service sendingWorker;
    
//    public AddVertexToWorker( int partId, Vertex vertex, Worker sendingWorker )
    public AddVertexToWorker( int partId, String vertexString, Service sendingWorker )
    {
        this.partId = partId;
//        this.vertex = vertex;
        this.vertexString = vertexString;
        this.sendingWorker = sendingWorker;
    }

    @Override
    public void execute( Proxy proxy ) { proxy.sendCommand( this ); }

    @Override
    public void execute( ServiceImpl serviceImpl ) throws Exception 
    {
        Worker worker = (Worker) serviceImpl;
//        worker.addVertexToWorker( partId, vertex, sendingWorker );
        worker.addVertexToWorker( partId, vertexString, sendingWorker );
    }
}
