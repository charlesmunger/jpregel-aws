package JpAws;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import datameer.awstasks.aws.ec2.InstanceGroupImpl;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This class is used to launch a machine running a Master thread.
 *
 * @author charlesmunger
 */
public class MasterMachines extends Ec2Machine {
    private final AmazonEC2 ec2 = new AmazonEC2Client(PregelAuthenticator.get());

    @Override
    public String[] start(int numWorkers) throws IOException {
        
         instanceGroup = new InstanceGroupImpl(ec2);

        RunInstancesRequest launchConfiguration = new RunInstancesRequest(Machine.AMIID, 1, 1)
                    .withKeyName(PregelAuthenticator.get().getMasterPrivateKeyName())
                    .withInstanceType("t1.micro").withSecurityGroupIds("quicklaunch-1");
                    System.out.println(launchConfiguration.toString());

        Reservation rs = instanceGroup.launch(launchConfiguration, TimeUnit.MINUTES, 5);
        List<Instance> instances = rs.getInstances();
        String privateDns = instances.get(0).getPrivateDnsName();
        String publicDns = instances.get(0).getPublicDnsName();
        
        MasterThread runMaster = new MasterThread(instanceGroup);
        runMaster.start();

        System.out.println("Public DNS: " + publicDns);
        System.out.println("Private DNS: " + privateDns);

        String[] Dns = {publicDns, privateDns};

        return Dns;
    }
}
