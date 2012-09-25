package JpAws;

import api.Cluster;
import api.MachineGroup;
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
import system.ClientToMaster;
import system.ReservationServiceImpl;
import system.Worker;

/**
 *
 * @author charlesmunger
 */
public class Ec2ReservationService extends ReservationServiceImpl
{

    /**
     * This is the AMI-ID for the machine to initialize as masters and workers.
     * Currently, it uses the standard Amazon Linux image.
     */
    public static final String AMIID = "ami-e565ba8c";
    /**
     * This is the AMI-ID for Cluster Compute instances - they need an HVM image.
     */
    public static final String HVM_AMIID = "ami-0da96764";
    /**
     * This is the name of the security group for EC2 instances to use.
     */
    public static final String SECURITY_GROUP = "jpregelgroup";
    private final AmazonEC2 ec2 = new AmazonEC2Client(PregelAuthenticator.get());
    private final Map<String, String> heapSizeMap = new HashMap<String, String>();

    public Ec2ReservationService()
    {
        heapSizeMap.put("m1.small", "-Xmx1600m -Xms1600m  ");
        heapSizeMap.put("m1.medium", "-Xmx3600m -Xms3600m ");
        heapSizeMap.put("cc2.8xlarge","-Xmx58000m -Xms58000m -XX:+UseConcMarkSweepGC"); //on machines with more than 2 cores, drastically improves performance. 
        heapSizeMap.put("m1.large","-Xmx7100m -Xms7100m");
        DescribeSecurityGroupsResult describeSecurityGroups = null;
        final DescribeSecurityGroupsRequest req = new DescribeSecurityGroupsRequest().withGroupNames(SECURITY_GROUP);
        try
        {
            describeSecurityGroups = ec2.describeSecurityGroups(req);
        } catch (AmazonServiceException e)
        {
            ec2.createSecurityGroup(new CreateSecurityGroupRequest(SECURITY_GROUP, "Created programatically by jpregel-aws"));
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
            ec2.authorizeSecurityGroupIngress(new AuthorizeSecurityGroupIngressRequest(SECURITY_GROUP, Collections.singletonList(p)));
        }
    }

    public static Cluster  newSmallCluster(int numWorkers) throws Exception
    {
        Ec2ReservationService rs = new Ec2ReservationService();
        return new Cluster(rs,"m1.small","m1.small",numWorkers);
    }

    public static Cluster newMassiveCluster(int numWorkers) throws Exception {
        Ec2ReservationService rs = new Ec2ReservationService();
        return new Cluster(rs,"cc2.8xlarge","cc2.8xlarge",numWorkers);
    }
    
    @Override
    public MachineGroup<Worker> callWorker(String instanceType, int numWorkers)
    {
        InstanceGroupImpl instanceGroup = new InstanceGroupImpl(ec2);
        RunInstancesRequest launchConfiguration = new RunInstancesRequest(instanceType.contains("cc") ? HVM_AMIID:AMIID, numWorkers, numWorkers)
                .withKeyName(PregelAuthenticator.getPrivateKeyName())
                .withInstanceType(instanceType)
                .withSecurityGroupIds(SECURITY_GROUP);
        System.out.println(launchConfiguration.toString());

        instanceGroup.launch(launchConfiguration, TimeUnit.MINUTES, 5);
        return new Ec2WorkerMachineGroup(instanceGroup, heapSizeMap.get(instanceType));
    }

    @Override
    public MachineGroup<ClientToMaster> callMaster(String instanceType)
    {
        InstanceGroupImpl instanceGroup = new InstanceGroupImpl(ec2);
        RunInstancesRequest launchConfiguration = new RunInstancesRequest(instanceType.contains("cc") ? HVM_AMIID:AMIID, 1, 1)
                .withKeyName(PregelAuthenticator.getMasterPrivateKeyName())
                .withInstanceType(instanceType)
                .withSecurityGroupIds(SECURITY_GROUP);
        System.out.println(launchConfiguration.toString());
        instanceGroup.launch(launchConfiguration, TimeUnit.MINUTES, 5);
        return new Ec2MasterMachineGroup(instanceGroup, heapSizeMap.get(instanceType));
    }
}
