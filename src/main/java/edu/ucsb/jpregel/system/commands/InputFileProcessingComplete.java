package edu.ucsb.jpregel.system.commands;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import jicosfoundation.Command;
import jicosfoundation.Proxy;
import edu.ucsb.jpregel.system.Master;

/**
 *
 * @author Pete Cappello
 */
public class InputFileProcessingComplete implements Command<Master>
{
    private int workerNum;
    private int numVertices;
    
//    public InputFileProcessingComplete(){}
    public InputFileProcessingComplete( int workerNum, int numVertices )
    { 
        this.workerNum = workerNum;
        this.numVertices = numVertices;
    }

    @Override
    public void execute(Proxy proxy) { proxy.sendCommand( this ); }

    @Override
    public void execute( Master master ) throws Exception 
    {
        master.inputFileProcessingComplete( workerNum, numVertices );
    }

    @Override
    public void writeExternal( ObjectOutput oo ) throws IOException
    {
        oo.writeInt(workerNum);
        oo.writeInt(numVertices);
    }

    @Override
    public void readExternal( ObjectInput oi ) throws IOException, ClassNotFoundException
    {
        workerNum = oi.readInt();
        numVertices = oi.readInt();
    }
}
