package api;

import java.io.IOException;
import java.io.Serializable;
import edu.ucsb.jpregel.system.FileSystem;
import edu.ucsb.jpregel.system.Worker;

/**
 *
 * @author Pete Cappello
 */
public interface WorkerOutputMaker extends Serializable
{
    void write( FileSystem fileSystem, Worker worker ) throws IOException;
}
