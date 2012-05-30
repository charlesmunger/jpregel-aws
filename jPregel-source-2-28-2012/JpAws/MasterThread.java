package JpAws;

import java.io.File;
import java.io.IOException;


import datameer.awstasks.aws.ec2.ssh.SshClient;
import datameer.awstasks.util.IoUtil;
import datameer.awstasks.aws.ec2.InstanceGroup;

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
     * Connects via SSH and runs the classpath script. 
     */
    public void run() {
        SshClient sshClient = null;
        sshClient = instanceGroup.createSshClient("ubuntu", privateKeyFile);

        try {
            //String cmd = "java -Djava.rmi.server.hostname=" + publicDns ;  
            //String cmd1 = cmd + " -Djava.security.policy=system/Permission.policy system.Master" ; 
            //String cmd2 = "javac system/Master.java" ;
            String cmd3 = "sh classpath_external.sh " + publicDns;
            //sshClient.executeCommand(maincmd,IoUtil.closeProtectedStream(System.out));
            sshClient.executeCommand(cmd3, IoUtil.closeProtectedStream(System.out));
            //sshClient.executeCommand(cmd1,IoUtil.closeProtectedStream(System.out));



        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
