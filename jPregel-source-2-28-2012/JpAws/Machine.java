package JpAws;

import com.xerox.amazonws.ec2.EC2Exception;
import java.io.IOException;

/**
 *
 * @author charlesmunger
 */
abstract public class Machine {

    /**
     *  Starts this machine, with n workers. 
     * @param numWorkers Specifies the number of worker threads. 
     * @param imageId Specifies the UID of the system 
     * @return
     * @throws EC2Exception
     * @throws IOException
     */
    abstract public String[] start(int numWorkers, String imageId) throws EC2Exception, IOException;

    /**
     * Stops the machine.
     * @throws EC2Exception
     * @throws IOException
     */
    abstract public void Stop() throws EC2Exception, IOException;
}
