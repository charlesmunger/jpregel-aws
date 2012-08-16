/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package JpAws;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import datameer.awstasks.aws.ec2.InstanceGroupImpl;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author charlesmunger
 */
public class SpotWorkerMachine extends Ec2Machine
{
private final AmazonEC2 ec2 = new AmazonEC2Client(PregelAuthenticator.get());
    @Override
    public String[] start(int numWorkers) throws IOException
    {
       instanceGroup = new InstanceGroupImpl(ec2);
        DescribeSecurityGroupsResult describeSecurityGroups = null;
        final DescribeSecurityGroupsRequest req = new DescribeSecurityGroupsRequest().withGroupNames(Ec2Machine.SECURITY_GROUP);
        try {
            describeSecurityGroups = ec2.describeSecurityGroups(req);
        } catch (AmazonServiceException e) {
            ec2.createSecurityGroup(new CreateSecurityGroupRequest(Ec2Machine.SECURITY_GROUP, "Created programatically by jpregel-aws"));
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
        return null;
    }
}
