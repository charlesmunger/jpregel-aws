package JpAws;

import java.io.IOException;

/**
 * This interface is for use by the cluster in starting and stopping individual EC2 instances.
 * @author charlesmunger
 */
public interface Machine {

    /**
     * This is the AMI-ID for the machine to initialize as masters and workers. 
     * Currently, it uses the standard Amazon Linux image.
     */
    public static final String AMIID = "ami-e565ba8c";

    /**
     * Starts this machine, with n workers.
     *
     * @param numWorkers Specifies the number of worker threads.
     * @param imageId Specifies the UID of the system image to start.
     * @return An array of DNS entries. The first element is public, the second
     * private.
     * @throws IOException
     */
    public String[] start(int numWorkers, String imageId) throws IOException;

    /**
     * Stops the machine.
     *
     * @throws IOException
     */
    public void Stop() throws IOException;
}
