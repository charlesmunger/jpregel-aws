package JpAws;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import datameer.awstasks.aws.ec2.InstanceGroup;
import datameer.awstasks.aws.ec2.InstanceGroupImpl;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class WorkerMachines implements Machine {
    public static final String WORKER_SECURITY_GROUP = "quick-start-1";

    private String masterDomainName;
    private InstanceGroup instanceGroup;

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
