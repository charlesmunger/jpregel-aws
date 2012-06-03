package JpAws;

import datameer.awstasks.aws.ec2.InstanceGroup;
import datameer.awstasks.aws.ec2.ssh.SshClient;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class WorkerThread extends Thread {
    public static final String JARNAME = "jpregel-aws.jar";
    private InstanceGroup instanceGroup;
    private String masterDomainName;
    File privateKeyFile = new File("mungerkey.pem");

    public WorkerThread(InstanceGroup instanceGroup, String masterDomainName) {
        this.instanceGroup = instanceGroup;
        this.masterDomainName = masterDomainName;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(15000);
        } catch (InterruptedException ex) {
            Logger.getLogger(WorkerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        SshClient sshClient = instanceGroup.createSshClient("ec2-user", privateKeyFile);
        File jars = new File("jars.tar");
        if(!jars.exists()) {
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
                System.out.println("Error uploading jar");
                System.exit(1);
            }
        } else if(distjar.exists()) {
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
            sshClient.uploadFile(new File("key.AWSkey"), "~/key.AWSkey");
            sshClient.uploadFile(new File("policy"), "~/policy");
            sshClient.executeCommand("tar -xvf jars.tar", null);
            sshClient.executeCommand("java -cp " + JARNAME + ":./dist/lib/*"
                    + " -Djava.security.policy=policy"
                    //+ " -Djava.ext.dirs=dist/lib/ " 
                    + " system.Worker " + masterDomainName, System.out);
        } catch (IOException ex) {
            System.out.println("Unable to upload file.");
            System.exit(1);
        }
//        try {
//            sshClient.executeCommand("./classpath.sh " + masterDomainName, IoUtil.closeProtectedStream(System.out));
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }
}
