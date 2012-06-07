package JpAws;

import datameer.awstasks.aws.ec2.InstanceGroup;
import datameer.awstasks.aws.ec2.ssh.SshClient;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private String jobDirectoryName;
    File privateKeyFile = new File("mungerkey.pem");

    /**
     * Creates a new MasterThread
     *
     * @param instanceGroup The instance group (with only one item) that the Master should be run on
     * @param jobDirectoryName  The name of the S3 bucket to store the files needed by and created
     * by the computation. 
     */
    public MasterThread(InstanceGroup instanceGroup, String jobDirectoryName) {
        this.instanceGroup = instanceGroup;
        this.jobDirectoryName = jobDirectoryName;
    }

    /**
     * Connects via SSH and runs the Master.
     */
    @Override
    public void run() {
        try {
            System.out.println("Waiting");
            Thread.sleep(30000);
            System.out.println("Waking");
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
            //sshClient.uploadFile(new File("1"), "~/1");
            //sshClient.executeCommand("mkdir "+jobDirectoryName +" ; "+"cd "+jobDirectoryName + " ; mkdir in ; cd ; mv 1 "+jobDirectoryName + "/in/1", null);
            //sshClient.executeCommand("mkdir "+jobDirectoryName +" ; "+"cd "+jobDirectoryName + " ; mkdir in ; cd ;", null);
            
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
    }
}
