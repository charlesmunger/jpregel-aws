package JpAws;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import aws.datameer.awstasks.aws.ec2.InstanceGroup;
import aws.datameer.awstasks.aws.ec2.InstanceGroupImpl;

import com.xerox.amazonws.ec2.EC2Exception;
import com.xerox.amazonws.ec2.Jec2;
import com.xerox.amazonws.ec2.LaunchConfiguration;
import com.xerox.amazonws.ec2.ReservationDescription;

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
        String accessKeyId = "AKIAIEINGU5VPVEQ4DAA";
        String accessKeySecret = "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";
        String privateKeyName = "varshap";

        for (int i = 1; i <= numWorkers; i++) {
            Jec2 ec2 = new Jec2(accessKeyId, accessKeySecret);
            InstanceGroup instanceGroup = new InstanceGroupImpl(ec2);

            LaunchConfiguration launchConfiguration = new LaunchConfiguration(imageId, 1, 1);
            launchConfiguration.setKeyName(privateKeyName);
            ReservationDescription rs = instanceGroup.startup(launchConfiguration, TimeUnit.MINUTES, 5);

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
