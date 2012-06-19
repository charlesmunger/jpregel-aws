package JpAws;

import datameer.awstasks.aws.ec2.InstanceGroup;
import datameer.awstasks.aws.ec2.ssh.SshClient;
import java.io.File;
import java.io.IOException;

/**
 * This class is used to asynchronously start a group of workers.
 *
 * @author charlesmunger
 */
public class WorkerThread extends Thread {

    /**
     * This refers to the name of the Jar containing the Worker classes.
     */
    public static final String JARNAME = "jpregel-aws.jar";
    private InstanceGroup instanceGroup;
    private String masterDomainName;
    File privateKeyFile = PregelAuthenticator.get().getPrivateKey();

    /**
     * Creates a new worker thread to start a group of workers.
     * @param instanceGroup The instance group to start workers on.
     * @param masterDomainName The domain of the master to connect to. 
     */
    public WorkerThread(InstanceGroup instanceGroup, String masterDomainName) {
        this.instanceGroup = instanceGroup;
        this.masterDomainName = masterDomainName;
    }

    /**
     * SSHs into every host in the worker instance group, uploads files, and
     * then starts the Worker class pointed at the master's domain name.
     */
    @Override
    public void run() {
        File jars = new File("jars.tar");
        if (!jars.exists()) {
            try {
                Runtime.getRuntime().exec("tar -zcvf jars.tar ./dist/lib");
            } catch (IOException ex) {
                System.out.println("Error tarring jars.");
                System.exit(1);
            }
        }
        try {
            Thread.sleep(30000);
        } catch (InterruptedException ex) {
            System.out.println("Waiting interrupted");
        }
        SshClient sshClient = instanceGroup.createSshClient("ec2-user", privateKeyFile, false);
        File thisjar = new File(JARNAME);
        File distjar = new File("dist/" + JARNAME);
        if (thisjar.exists()) {
            try {
                sshClient.uploadFile(thisjar, "~/" + JARNAME);
            } catch (IOException ex) {
                System.out.println("Error uploading jar");
                System.exit(1);
            }
        } else if (distjar.exists()) {
            try {
                sshClient.uploadFile(distjar, "~/" + JARNAME);
            } catch (IOException ex) {
                System.out.println("Error uploading distjar");
                System.exit(1);
            }
        } else {
            System.err.println("Didn't find jar in " + distjar.getAbsolutePath() + " or " + thisjar.getAbsolutePath());
            System.exit(1);
        }
        try {
            sshClient.uploadFile(jars, "~/jars.tar");
            //sshClient.uploadFile(new File("key.AWSkey"), "~/key.AWSkey");
            //sshClient.uploadFile(new File("policy"), "~/policy");
            sshClient.executeCommand("tar -zxvf jars.tar", null);
            System.out.println("Returned!");
        } catch (IOException ex) {
            System.out.println("Unable to upload file." + ex.getLocalizedMessage());
            System.exit(1);
        }
        try {
            sshClient.executeCommand("java -cp " + JARNAME + ":./dist/lib/*"
                        + " -Djava.security.policy=policy"
                        + " system.Worker " + masterDomainName, null);
        } catch (IOException ex) {
            System.out.println("Workers terminated");
        }
    }
}
