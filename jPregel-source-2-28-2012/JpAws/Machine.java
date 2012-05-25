package JpAws;



import java.io.IOException;
import com.xerox.amazonws.ec2.EC2Exception;

abstract public class Machine {
	
	abstract public String[] start(int numWorkers, String imageId) throws EC2Exception, IOException ; 
	
	abstract public void Stop() throws EC2Exception, IOException ; 
		
	}
	


