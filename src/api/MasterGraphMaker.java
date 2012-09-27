package api;

import system.FileSystem;

//     Put this code in the subclasses of FileSystem. Then, invoke the 
//     "openInputBuffer" method (or whatever it will be called).

/**
 * Each jpregel Job specifies a MasterGraphMaker.
 * <p>
 * A MasterGraphMaker
 * <ol>
 *     <li>optionally reads the <i>input</i> file;</li>
 *     <li>produces an input file for each Worker: 
 *         The file for Worker number <i>n</i> is named <i>n</i>
 *         and resides in the <i>in</i> directory.
 *     </li>
 * </ol>
 * </p>
 *
 * @author Pete Cappello
 */
public interface MasterGraphMaker extends java.io.Serializable
{
    /**
     * Make the Worker input files.
     * 
     * @param fileSystem a reference to a FileSystem object through which files are read/written.
     * @param numWorkers the number of worker machines
     * @return number of vertices in graph
     */
    void make( FileSystem fileSystem, int numWorkers );
}
