package JpAws;

import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import aws.datameer.awstasks.aws.ec2.InstanceGroup;
import aws.datameer.awstasks.aws.ec2.InstanceGroupImpl;
import com.xerox.amazonws.ec2.EC2Exception;
import com.xerox.amazonws.ec2.Jec2;
import com.xerox.amazonws.ec2.LaunchConfiguration;
import com.xerox.amazonws.ec2.ReservationDescription;

/**
 * This class is used to launch a machine running a Master thread.
 *
 * @author charlesmunger
 */
public class MasterMachines extends Machine {

    @Override
    public String[] start(int numWorkers, String imageId) throws EC2Exception,
            IOException {
        String accessKeyId = "AKIAIEINGU5VPVEQ4DAA";
        String accessKeySecret = "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";
        String privateKeyName = "varshap";
        String ipAddr = "";

        Jec2 ec2 = new Jec2(accessKeyId, accessKeySecret);
        InstanceGroup instanceGroup = new InstanceGroupImpl(ec2);

        LaunchConfiguration launchConfiguration = new LaunchConfiguration(imageId, 1, 1);
        launchConfiguration.setKeyName(privateKeyName);
        ReservationDescription rs = instanceGroup.startup(launchConfiguration, TimeUnit.MINUTES, 5);

        List list = (List) rs.getInstances();
        String instanceString = list.toString();
        System.out.println("ins" + instanceString);


        StringTokenizer st = new StringTokenizer(instanceString);
        String privateDns = "", publicDns = "";
        int i = 0;

        while (st.hasMoreTokens()) {
            st.nextToken();
            i++;
            if (i == 2) {
                privateDns = st.nextToken();
                privateDns = privateDns.substring(11, privateDns.length() - 1);
                publicDns = st.nextToken();
                publicDns = publicDns.substring(4, publicDns.length() - 1);

            }
        }
        MasterThread runMaster = new MasterThread(instanceGroup, publicDns);
        runMaster.start();

        System.out.println("here" + publicDns);
        System.out.println("here" + privateDns);

        String[] Dns = {publicDns, privateDns};

        return Dns;
    }

    @Override
    public void Stop() throws EC2Exception, IOException {
    }
}
