package api;

import edu.ucsb.jpregel.system.Worker;

/**
 *
 * @author Pete Cappello
 */
public abstract class WorkerGraphMaker implements java.io.Serializable
{
    abstract public int makeGraph( Worker worker );
    
    public int getWorkerNum( int partId, int numWorkers )
    {
        return ( partId % numWorkers ) + 1;
    }
}
