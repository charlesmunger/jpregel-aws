package system;

/**
 *
 * @author Pete Cappello
 */
public interface MasterGraphMaker extends java.io.Serializable
{
    void make( FileSystem fileSystem, int numWorkers );
}
