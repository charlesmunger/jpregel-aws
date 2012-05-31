package JpAws;

import datameer.awstasks.aws.ec2.InstanceGroup;
import datameer.awstasks.aws.ec2.ssh.SshClient;
import datameer.awstasks.util.IoUtil;
import java.io.File;
import java.io.IOException;

/**
 * This class is used for asynchronously running the classpath_external script.
 * @author charlesmunger
 */
public class MasterThread extends Thread {

    private InstanceGroup instanceGroup;
    private String publicDns;
    File privateKeyFile = new File("/home/varsha/Desktop/varshap.pem");

    /**
     * Creates a new MasterThread 
     * @param instanceGroup The instance group to 
     * @param publicDns
     */
    public MasterThread(InstanceGroup instanceGroup, String publicDns) {
        this.instanceGroup = instanceGroup;
        this.publicDns = publicDns;
    }

    /**
     * Connects via SSH and runs the classpath_external script. 
     */
    public void run() {
        SshClient sshClient = instanceGroup.createSshClient("ubuntu", privateKeyFile);
        try {
            sshClient.executeCommand("sh classpath_external.sh " + publicDns, IoUtil.closeProtectedStream(System.out));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
