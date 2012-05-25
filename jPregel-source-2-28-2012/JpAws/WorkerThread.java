package JpAws;

import java.io.File;
import java.io.IOException;

import aws.datameer.awstasks.aws.ec2.InstanceGroup;
import aws.datameer.awstasks.aws.ec2.ssh.SshClient;
import aws.datameer.awstasks.util.IoUtil;

import com.xerox.amazonws.ec2.EC2Exception;

public class WorkerThread extends Thread {
	private InstanceGroup instanceGroup;
	private String masterDomainName ; 
	File privateKeyFile = new File("/home/ubuntu/varshap.pem") ; 

	public WorkerThread(InstanceGroup instanceGroup, String masterDomainName)
	{
		this.instanceGroup = instanceGroup;
		this.masterDomainName = masterDomainName ; 
	}
	public void run()
	{
		SshClient sshClient = null;
		try {
			sshClient = instanceGroup.createSshClient("ubuntu", privateKeyFile);
		} catch (EC2Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        try {
        	String cmd = "./classpath.sh " + masterDomainName ;
			sshClient.executeCommand(cmd,IoUtil.closeProtectedStream(System.out));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

}
