package api;

import system.FileSystem;

/**
 * Each jpregel Job specifies a MasterOutputMaker.
 * <p>
 * A MasterOutputMaker
 * <ol>
 *     <li>optionally reads the worker output files in the <i>out</i> directory,
 *         where the output file from Worker number <i>n</i> is named <i>n</i>;
 *     </li>
 *     <li>produces the output file, whose file name is <i>output</i>.</li>
 * </ol>
 * </p>
 *
 * @author Peter Cappello
 */
public interface MasterOutputMaker extends java.io.Serializable
{
    /**
     * Write the Master output file.
     * 
     * @param fileSystem the FileSystem object used to read the Worker files
     *        and write the Master output file.
     * @param numWorkers the number or Workers.
     */
    void write( FileSystem fileSystem, int numWorkers );   
}
