package JpAws;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import datameer.awstasks.aws.ec2.InstanceGroup;
import datameer.awstasks.aws.ec2.InstanceGroupImpl;
import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

/**
 * This class is used to launch a machine running a Master thread.
 *
 * @author charlesmunger
 */
public class MasterMachines implements Machine {

    @Override
    public String[] start(int numWorkers, String imageId) throws IOException {
        //String accessKeyId = 
        //String accessKeySecret = 
        String privateKeyName = "mungerkey";

        AmazonEC2 ec2 = new AmazonEC2Client(PregelAuthenticator.get());
        InstanceGroup instanceGroup = new InstanceGroupImpl(ec2);

        RunInstancesRequest launchConfiguration = new RunInstancesRequest(imageId, 1, 1)
                    .withKeyName(privateKeyName)
                    .withInstanceType("t1.micro").withSecurityGroupIds("quick-start-1");
        Reservation rs = instanceGroup.launch(launchConfiguration, TimeUnit.MINUTES, 5);
        List<Instance> instances = rs.getInstances();
        //List list = (List) rs.getInstances();
        String privateDns = instances.get(0).getPrivateDnsName();
        String publicDns = instances.get(0).getPublicDnsName();
        
//        String instanceString = list.toString();
//        System.out.println("ins" + instanceString);


//        StringTokenizer st = new StringTokenizer(instanceString);
//        String privateDns = "", publicDns = "";
          
//        int i = 0;
//        
//        while (st.hasMoreTokens()) {
//            st.nextToken();
//            i++;
//            if (i == 2) {
//                privateDns = st.nextToken();
//                System.out.println(privateDns);
//                privateDns = privateDns.substring(11, privateDns.length() - 1);
//                publicDns = st.nextToken();
//                publicDns = publicDns.substring(4, publicDns.length() - 1);
//
//            }
//        }
        MasterThread runMaster = new MasterThread(instanceGroup, publicDns);
        runMaster.start();

        System.out.println("here" + publicDns);
        System.out.println("here" + privateDns);

        String[] Dns = {publicDns, privateDns};

        return Dns;
    }

    @Override
    public void Stop() throws IOException {
        //TODO make this useful
    }
}
