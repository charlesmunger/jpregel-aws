package api;

import system.FileSystem;

//     Put this code in the subclasses of FileSystem. Then, invoke the 
//     "openInputBuffer" method (or whatever it will be called).

/**
 * <p>Each jpregel Job specifies a MasterGraphMaker</p>
 * <p>
 * A MasterGraphMaker reads the <i>input</i> file and produces an input file
 * for each Worker: The file for Worker number <i>n</i> is named <i>n</i>
 * and resides in the <in> directory.</p>
 *
 * @author Pete Cappello
 */
public interface MasterGraphMaker extends java.io.Serializable
{
    /*
     * @return number of vertices in graph
     */
    void make( FileSystem fileSystem, int numWorkers );
}
