package system;

/**
 *
 * @author Peter Cappello
 */
public interface Writer extends java.io.Serializable
{
    void write( FileSystem fileSystem, int numWorkers );   
}
