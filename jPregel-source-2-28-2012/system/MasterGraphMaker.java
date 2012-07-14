package system;

/**
 * To do:
 * 1. Factor out conditional (isEc2) from file opening in make method.
 *    Put this code in the subclasses of FileSystem. Then, invoke the 
 *    "openInputBuffer" method (or whatever it will be called).
 *
 * @author Pete Cappello
 */
public interface MasterGraphMaker extends java.io.Serializable
{
    void make( FileSystem fileSystem, int numWorkers );
}
