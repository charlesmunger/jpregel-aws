package api;

import edu.ucsb.jpregel.system.FileSystem;

/**
 * The MasterGraphMaker makes a file for each WorkerGraphMaker that indicates
 * what portion of the graph to make.
 * Each jpregel Job specifies a MasterGraphMaker.
 * <p>
 * A MasterGraphMaker
 * <ol>
 *     <li>optionally reads the job <b>input</b> file;</li>
 *     <li>makes an input file for each Worker: 
 *         The file for Worker number <i>n</i> is named <b>n</b>
 *         and resides in the <b>in</b> directory.
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
