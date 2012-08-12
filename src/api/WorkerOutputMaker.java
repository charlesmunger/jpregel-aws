package api;

import java.io.IOException;
import java.io.Serializable;
import system.FileSystem;
import system.Worker;

/**
 *
 * @author Pete Cappello
 */
public interface WorkerOutputMaker extends Serializable
{
    void write( FileSystem fileSystem, Worker worker ) throws IOException;
}
