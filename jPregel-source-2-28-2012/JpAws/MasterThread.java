package JpAws;

import datameer.awstasks.aws.ec2.InstanceGroup;
import datameer.awstasks.aws.ec2.ssh.SshClient;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is used for asynchronously running the classpath_external script.
 *
 * @author charlesmunger
 */
public class MasterThread extends Thread {

    public static final String JARNAME = "jpregel-aws.jar";
    private InstanceGroup instanceGroup;
    private String publicDns;
    File privateKeyFile = new File("mungerkey.pem");

    /**
     * Creates a new MasterThread
     *
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
        try {
            Thread.sleep(6000);
        } catch (InterruptedException ex) {
            Logger.getLogger(MasterThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        SshClient sshClient = instanceGroup.createSshClient("ec2-user", privateKeyFile);
        File jars = new File("jars.tar");
        if (!jars.exists()) {
            try {
                Runtime.getRuntime().exec("tar -cvf jars.tar ./dist/lib");
            } catch (IOException ex) {
                System.out.println("Error tarring jars.");
                System.exit(1);
            }
        }
        try {
            sshClient.uploadFile(new File(JARNAME), "~/" + JARNAME);
            sshClient.uploadFile(jars, "~/jars.tar");
            sshClient.uploadFile(new File("policy"), "~/policy");
            sshClient.uploadFile(new File("key.AWSkey"), "~/key.AWSkey");
            sshClient.uploadFile(privateKeyFile, "~/"+privateKeyFile.getName());
            sshClient.executeCommand("tar -xvf jars.tar", null);
            sshClient.executeCommand("java -cp " + JARNAME + ":./dist/lib/*"
                    + " -Djava.security.policy=policy"
                    //+ " -Djava.ext.dirs=dist/lib/ " 
                    + " system.Master", System.out);
        } catch (IOException ex) {
            System.out.println("Unable to upload file.");
            System.exit(1);
        }

//        SshClient sshClient = instanceGroup.createSshClient("ubuntu", privateKeyFile);
//        try {
//            sshClient.executeCommand("sh classpath_external.sh " + publicDns, IoUtil.closeProtectedStream(System.out));
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }
}
