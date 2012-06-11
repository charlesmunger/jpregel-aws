package JpAws;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import datameer.awstasks.aws.ec2.InstanceGroup;
import datameer.awstasks.aws.ec2.InstanceGroupImpl;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * This class is used for spawning groups of workers.
 * @author charlesmunger
 */
public class WorkerMachines implements Machine {
    /**
     * This string represents the identifier of the EC2 security group to associate machines with.
     */
    public static final String WORKER_SECURITY_GROUP = "quick-start-1";

    private String masterDomainName;
    private InstanceGroup instanceGroup;

    /**
     * This creates a new workerMachines object, which is a local interface for managing groups of
     * workers. 
     * @param masterDomainName Specifies the hostname of the Master, for the workers to connect
     * to. 
     */
    public WorkerMachines(String masterDomainName) {
        this.masterDomainName = masterDomainName;
    }

    @Override
    public String[] start(int numWorkers) throws IOException {
        AmazonEC2 ec2 = new AmazonEC2Client(PregelAuthenticator.get());
        instanceGroup = new InstanceGroupImpl(ec2);

        RunInstancesRequest launchConfiguration = new RunInstancesRequest(Machine.AMIID, numWorkers, numWorkers)
                .withKeyName(PregelAuthenticator.get().getPrivateKeyName())
                .withInstanceType("t1.micro")
                .withSecurityGroupIds(WORKER_SECURITY_GROUP);
        System.out.println(launchConfiguration.toString());
        
        instanceGroup.launch(launchConfiguration, TimeUnit.MINUTES, 5);
        WorkerThread runWorker = new WorkerThread(instanceGroup, masterDomainName);
        runWorker.start();

        return null;
    }

    @Override
    public void Stop() throws IOException {
        instanceGroup.terminate();
    }
}
