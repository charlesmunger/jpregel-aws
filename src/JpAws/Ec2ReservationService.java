package JpAws;

import api.MachineGroup;
import api.ReservationService;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import datameer.awstasks.aws.ec2.InstanceGroupImpl;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author charlesmunger
 */
public class Ec2ReservationService implements ReservationService
{

    private AmazonEC2 ec2 = new AmazonEC2Client(PregelAuthenticator.get());
    private Map<String, String> heapSizeMap = new HashMap<String, String>();

    public Ec2ReservationService()
    {
        DescribeSecurityGroupsResult describeSecurityGroups = null;
        final DescribeSecurityGroupsRequest req = new DescribeSecurityGroupsRequest().withGroupNames(Ec2Machine.SECURITY_GROUP);
        try
        {
            describeSecurityGroups = ec2.describeSecurityGroups(req);
        } catch (AmazonServiceException e)
        {
            ec2.createSecurityGroup(new CreateSecurityGroupRequest(Ec2Machine.SECURITY_GROUP, "Created programatically by jpregel-aws"));
            describeSecurityGroups = ec2.describeSecurityGroups(req);
        }
        List<IpPermission> ipPermissions = describeSecurityGroups.getSecurityGroups().get(0).getIpPermissions();
        IpPermission p = new IpPermission();
        p.setIpProtocol("tcp");
        p.setFromPort(0);
        p.setToPort(65535);
        p.setIpRanges(Collections.singleton("0.0.0.0/0"));
        if (!ipPermissions.contains(p))
        {
            ec2.authorizeSecurityGroupIngress(new AuthorizeSecurityGroupIngressRequest(Ec2Machine.SECURITY_GROUP, Collections.singletonList(p)));
        }
    }

    @Override
    public MachineGroup reserveWorkers(String instanceType, int numWorkers)
    {
        InstanceGroupImpl instanceGroup = new InstanceGroupImpl(ec2);

        RunInstancesRequest launchConfiguration = new RunInstancesRequest(Machine.AMIID, numWorkers, numWorkers).withKeyName(PregelAuthenticator.getPrivateKeyName()).withInstanceType(instanceType).withSecurityGroupIds(Ec2Machine.SECURITY_GROUP);
        System.out.println(launchConfiguration.toString());

        instanceGroup.launch(launchConfiguration, TimeUnit.MINUTES, 5);
        return new Ec2WorkerMachineGroup(instanceGroup, heapSizeMap.get(instanceType));
    }

    @Override
    public MachineGroup reserveMaster(String instanceType)
    {
        InstanceGroupImpl instanceGroup = new InstanceGroupImpl(ec2);
        RunInstancesRequest launchConfiguration = new RunInstancesRequest(Machine.AMIID, 1, 1).withKeyName(PregelAuthenticator.getMasterPrivateKeyName()).withInstanceType(instanceType).withSecurityGroupIds(Ec2Machine.SECURITY_GROUP);
        System.out.println(launchConfiguration.toString());

        Reservation rs = instanceGroup.launch(launchConfiguration, TimeUnit.MINUTES, 5);
        return new Ec2MasterMachineGroup(instanceGroup, heapSizeMap.get(instanceType));
    }
}
