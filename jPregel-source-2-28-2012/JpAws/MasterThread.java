package JpAws;

import datameer.awstasks.aws.ec2.InstanceGroup;
import datameer.awstasks.aws.ec2.ssh.SshClient;
import java.io.File;
import java.io.IOException;

/**
 * This class is used for asynchronously deploying and running the Master class.
 *
 * @author charlesmunger
 */
public class MasterThread extends Thread {

    /**
     * This specifies the filename of the jar containing the main class for this project.
     */
    public static final String JARNAME = "jpregel-aws.jar";
    private InstanceGroup instanceGroup;
    
    /**
     * Creates a new MasterThread
     *
     * @param instanceGroup The instance group (with only one item) that the Master should be run on
     */
    public MasterThread(InstanceGroup instanceGroup) {
        this.instanceGroup = instanceGroup;
    }

    /**
     * Connects via SSH and runs the Master.
     */
    @Override
    public void run() {
        File privateKeyFile = PregelAuthenticator.get().getPrivateKey();
        try {
            System.out.println("Waiting");
            Thread.sleep(15000);
            System.out.println("Waking");
        } catch (InterruptedException ex) {
            System.out.println("Waiting interrupted.");
        }
        SshClient sshClient = instanceGroup.createSshClient("ec2-user", PregelAuthenticator.get().getMasterPrivateKey());
        File jars = new File("jars.tar");
        if (!jars.exists()) {
            try {
                Runtime.getRuntime().exec("tar -zcvf jars.tar ./dist/lib");
            } catch (IOException ex) {
                System.out.println("Error tarring jars.");
                System.exit(1);
            }
        }
        File thisjar = new File(JARNAME);
        File distjar = new File("dist/"+JARNAME);
        if(thisjar.exists()) {
            try {
                sshClient.uploadFile(thisjar, "~/" + JARNAME);
            } catch (IOException ex) {
                System.err.println("Error uploading jar");
                System.exit(1);
            }
        } else if(distjar.exists()) {
            try {
                sshClient.uploadFile(distjar, "~/" + JARNAME);
            } catch (IOException ex) {
                System.err.println("Error uploading distjar");
                System.exit(1);
            }
        } else {
            System.err.println("Didn't find jar in " + distjar.getAbsolutePath() + " or " + thisjar.getAbsolutePath());
        }
        try {
            sshClient.uploadFile(jars, "~/jars.tar");
            sshClient.uploadFile(new File("policy"), "~/policy");
            sshClient.uploadFile(new File("key.AWSkey"), "~/key.AWSkey");
            sshClient.uploadFile(privateKeyFile, "~/"+privateKeyFile.getName());
            sshClient.executeCommand("tar -zxvf jars.tar", null);
        } catch (IOException ex) {
            System.out.println("Unable to upload file." + ex.getLocalizedMessage());
            System.exit(1);
        }
        try {
            sshClient.executeCommand("java -cp " + JARNAME + ":./dist/lib/*"
                        + " -Djava.security.policy=policy"
                        + " system.Master", System.out);
        } catch (IOException ex) {
            System.out.println("Master disconnected "+ex.getLocalizedMessage());
        }
    }
}
