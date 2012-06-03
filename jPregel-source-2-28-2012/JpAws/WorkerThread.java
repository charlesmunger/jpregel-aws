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
            Thread.sleep(60000);
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
        try {
            sshClient.uploadFile(new File("./dist/" + JARNAME), "~/"+JARNAME);
            sshClient.uploadFile(jars, "~/jars.tar");
            sshClient.executeCommand("tar -xvf jars.tar", System.out);
            sshClient.executeCommand("java -cp "+JARNAME + " system.Worker" + " "+ masterDomainName, System.out);
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
