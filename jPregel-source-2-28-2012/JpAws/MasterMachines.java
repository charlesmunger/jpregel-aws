package JpAws;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import datameer.awstasks.aws.ec2.InstanceGroupImpl;
import java.io.IOException;
import java.util.Collections;
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
        CreateSecurityGroupResult createSecurityGroup;
        DescribeSecurityGroupsResult describeSecurityGroups = null;
        final DescribeSecurityGroupsRequest req = new DescribeSecurityGroupsRequest().withGroupNames(Ec2Machine.SECURITY_GROUP);;
        try {
            describeSecurityGroups = ec2.describeSecurityGroups(req);
        } catch (AmazonServiceException e) {
            createSecurityGroup = ec2.createSecurityGroup(new CreateSecurityGroupRequest(Ec2Machine.SECURITY_GROUP, "Created programatically by jpregel-aws"));
            describeSecurityGroups = ec2.describeSecurityGroups(req);
        }
        List<IpPermission> ipPermissions = describeSecurityGroups.getSecurityGroups().get(0).getIpPermissions();
        IpPermission p = new IpPermission();
        p.setIpProtocol("tcp");
        p.setFromPort(0);
        p.setToPort(65535);
        p.setIpRanges(Collections.singleton("0.0.0.0/0"));
        if(!ipPermissions.contains(p)) {
            ec2.authorizeSecurityGroupIngress(new AuthorizeSecurityGroupIngressRequest(Ec2Machine.SECURITY_GROUP, Collections.singletonList(p)));
        }
        RunInstancesRequest launchConfiguration = new RunInstancesRequest(Machine.AMIID, 1, 1)
                    .withKeyName(PregelAuthenticator.get().getMasterPrivateKeyName())
                    .withInstanceType("t1.small").withSecurityGroupIds(Ec2Machine.SECURITY_GROUP);
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
