package system;

import java.io.Serializable;

/**
 *
 * @author Pete Cappello
 */
public interface WorkerWriter extends Serializable
{
    void write( FileSystem fileSystem, Worker worker );
}
