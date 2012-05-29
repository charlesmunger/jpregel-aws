package JpAws;

import com.amazonaws.services.ec2.model.Reservation;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.xerox.amazonws.ec2.EC2Exception;
import datameer.awstasks.aws.ec2.InstanceGroup;
import datameer.awstasks.aws.ec2.InstanceGroupImpl;

public class WorkerMachines extends Machine {

    private String masterDomainName;

    public WorkerMachines(String masterDomainName) {
        this.masterDomainName = masterDomainName;
    }

    @Override
    public String[] start(int numWorkers, String imageId) throws EC2Exception,
            IOException {
        // TODO Auto-generated method stub
        String[] returnvalue = null;
        AmazonEC2 ec2 = new AmazonEC2Client(new AWSCredentials() {

            @Override
            public String getAWSAccessKeyId() {
                return "AKIAIEINGU5VPVEQ4DAA";
            }

            @Override
            public String getAWSSecretKey() {
                return "EIdITzPxbGOFsH/r9OVAOKJ7HJ+yPL4tKjiwxyrL";
            }
        });
        String privateKeyName = "varshap";

        for (int i = 1; i <= numWorkers; i++) {
            InstanceGroup instanceGroup = new InstanceGroupImpl(ec2);
            RunInstancesRequest launchConfiguration = new RunInstancesRequest(imageId, 1, 1);
            launchConfiguration.setKeyName(privateKeyName);
            Reservation rs = instanceGroup.launch(launchConfiguration, TimeUnit.MINUTES, 5);
            WorkerThread runWorker = new WorkerThread(instanceGroup, masterDomainName);
            runWorker.start();
        }

        return returnvalue;
    }

    @Override
    public void Stop() throws EC2Exception, IOException {
        // TODO Doesn't actually DO anything
    }
}
