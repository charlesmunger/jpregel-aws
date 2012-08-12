package api;

import system.FileSystem;

/**
 *
 * @author Peter Cappello
 */
public interface MasterOutputMaker extends java.io.Serializable
{
    void write( FileSystem fileSystem, int numWorkers );   
}
