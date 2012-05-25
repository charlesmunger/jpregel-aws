package JpAws;

import java.io.File;
import java.io.IOException;

import com.xerox.amazonws.ec2.EC2Exception;

import aws.datameer.awstasks.aws.ec2.ssh.SshClient;
import aws.datameer.awstasks.util.IoUtil;
import aws.datameer.awstasks.aws.ec2.InstanceGroup;

public class MasterThread extends Thread {

	private InstanceGroup instanceGroup;
	private String publicDns ; 
	File privateKeyFile = new File("/home/varsha/Desktop/varshap.pem") ; 

	public MasterThread(InstanceGroup instanceGroup, String publicDns)
	{
		this.instanceGroup = instanceGroup;
		this.publicDns = publicDns ; 
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
        	//String cmd = "java -Djava.rmi.server.hostname=" + publicDns ;  
        	//String cmd1 = cmd + " -Djava.security.policy=system/Permission.policy system.Master" ; 
        	//String cmd2 = "javac system/Master.java" ;
        	String cmd3 = "sh classpath_external.sh " + publicDns ;
			//sshClient.executeCommand(maincmd,IoUtil.closeProtectedStream(System.out));
			sshClient.executeCommand(cmd3,IoUtil.closeProtectedStream(System.out));
			//sshClient.executeCommand(cmd1,IoUtil.closeProtectedStream(System.out));

			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}
