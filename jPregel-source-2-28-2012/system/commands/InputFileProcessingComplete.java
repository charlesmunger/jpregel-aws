package system.commands;

import jicosfoundation.Command;
import jicosfoundation.Proxy;
import jicosfoundation.ServiceImpl;
import system.Master;

/**
 *
 * @author Pete Cappello
 */
public class InputFileProcessingComplete implements Command
{
    private int workerNum;
    private int numVertices;
    
    public InputFileProcessingComplete( int workerNum, int numVertices )
    { 
        this.workerNum = workerNum;
        this.numVertices = numVertices;
    }

    @Override
    public void execute(Proxy proxy) { proxy.sendCommand( this ); }

    @Override
    public void execute(ServiceImpl serviceImpl) throws Exception 
    {
        Master master = (Master) serviceImpl;
        master.inputFileProcessingComplete( workerNum, numVertices );
    }
}
