package JpAws;

import datameer.awstasks.aws.ec2.InstanceGroup;
import datameer.awstasks.aws.ec2.ssh.SshClient;
import datameer.awstasks.util.IoUtil;
import java.io.File;
import java.io.IOException;


public class WorkerThread extends Thread {

    private InstanceGroup instanceGroup;
    private String masterDomainName;
    File privateKeyFile = new File("mungerkey.pem");

    public WorkerThread(InstanceGroup instanceGroup, String masterDomainName) {
        this.instanceGroup = instanceGroup;
        this.masterDomainName = masterDomainName;
    }

    @Override
    public void run() {
        SshClient sshClient = instanceGroup.createSshClient("ec2-user", privateKeyFile);
        try {
            sshClient.executeCommand("./classpath.sh " + masterDomainName, IoUtil.closeProtectedStream(System.out));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
