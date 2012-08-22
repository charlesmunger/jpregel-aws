package JpAws;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
     * This is the name of the security group for EC2 instances to use.
     */
    public static final String SECURITY_GROUP = "jpregelgroup";
    private final AmazonEC2 ec2 = new AmazonEC2Client(PregelAuthenticator.get());
    private final Map<String, String> heapSizeMap = new HashMap<String, String>();
    private final ExecutorService exec = Executors.newCachedThreadPool();

    public Ec2ReservationService()
    {
        heapSizeMap.put("m1.small", "-Xmx1600m -Xms1600m  ");
        heapSizeMap.put("cc2.8xlarge","-xmx58000m -Xms58000m");
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

    public static ClientToMaster newSmallCluster(int numWorkers) throws Exception
    {
        Ec2ReservationService rs = new Ec2ReservationService();
        Future<MachineGroup<ClientToMaster>> masterMachine = rs.reserveMaster("m1.large");
        Future<MachineGroup<Worker>> workers = rs.reserveWorkers("m1.large", numWorkers);
        Future<ClientToMaster> deployMaster = masterMachine.get().deploy(Integer.toString(numWorkers));
        workers.get().deploy(masterMachine.get().getHostname());
        return deployMaster.get();
    }

    @Override
    public MachineGroup<Worker> callWorker(String instanceType, int numWorkers)
    {
        InstanceGroupImpl instanceGroup = new InstanceGroupImpl(ec2);

        RunInstancesRequest launchConfiguration = new RunInstancesRequest(AMIID, numWorkers, numWorkers)
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
        RunInstancesRequest launchConfiguration = new RunInstancesRequest(AMIID, 1, 1)
                .withKeyName(PregelAuthenticator.getMasterPrivateKeyName())
                .withInstanceType(instanceType)
                .withSecurityGroupIds(SECURITY_GROUP);
        System.out.println(launchConfiguration.toString());
        instanceGroup.launch(launchConfiguration, TimeUnit.MINUTES, 5);
        return new Ec2MasterMachineGroup(instanceGroup, heapSizeMap.get(instanceType));
    }
}
