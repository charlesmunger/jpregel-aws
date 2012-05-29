package JpAws;

import java.io.File;
import java.io.IOException;

import datameer.awstasks.aws.ec2.InstanceGroup;
import datameer.awstasks.aws.ec2.ssh.SshClient;
import datameer.awstasks.util.IoUtil;


public class WorkerThread extends Thread {

    private InstanceGroup instanceGroup;
    private String masterDomainName;
    File privateKeyFile = new File("/home/ubuntu/varshap.pem");

    public WorkerThread(InstanceGroup instanceGroup, String masterDomainName) {
        this.instanceGroup = instanceGroup;
        this.masterDomainName = masterDomainName;
    }

    @Override
    public void run() {
        SshClient sshClient = null;
        sshClient = instanceGroup.createSshClient("ubuntu", privateKeyFile);


        try {
            String cmd = "./classpath.sh " + masterDomainName;
            sshClient.executeCommand(cmd, IoUtil.closeProtectedStream(System.out));

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
