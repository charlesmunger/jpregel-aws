package api;

import edu.ucsb.jpregel.system.Worker;

/**
 *
 * @author Pete Cappello
 */
public interface WorkerGraphMaker extends java.io.Serializable
{
    int makeGraph( Worker worker );
}
