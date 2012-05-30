package JpAws;

import java.io.IOException;

/**
 *
 * @author charlesmunger
 */
interface Machine {

    /**
     *  Starts this machine, with n workers. 
     * @param numWorkers Specifies the number of worker threads. 
     * @param imageId Specifies the UID of the system image to start. 
     * @return An array of DNS entries. The first element is public, the second private. 
     * @throws IOException
     */
    abstract public String[] start(int numWorkers, String imageId) throws IOException;

    /**
     * Stops the machine.
     * @throws IOException
     */
    abstract public void Stop() throws IOException;
}
