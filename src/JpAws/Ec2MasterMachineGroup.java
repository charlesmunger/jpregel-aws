package JpAws;

import datameer.awstasks.aws.ec2.InstanceGroup;
import datameer.awstasks.aws.ec2.ssh.SshClient;
import java.io.File;
import java.io.IOException;
import system.ClientToMaster;

/**
 *
 * @author charlesmunger
 */
public class Ec2MasterMachineGroup extends Ec2MachineGroup<ClientToMaster>
{

    public static final String JARNAME = "jpregel-aws";

    public Ec2MasterMachineGroup(InstanceGroup i, String heapsize)
    {
        super(i, heapsize);
    }

    @Override
    void startObject(final String[] args)
    {
        File privateKeyFile = PregelAuthenticator.getPrivateKey();
        File jars = new File("jars.tar");
        if (!jars.exists())
        {
            try
            {
                Runtime.getRuntime().exec("tar -zcvf jars.tar ./dist/lib policy key.AWSkey");
            } catch (IOException ex)
            {
                System.out.println("Error tarring jars.");
                System.exit(1);
            }
        }
        try
        {
            System.out.println("Waiting");
            Thread.sleep(30000);
            System.out.println("Waking");
        } catch (InterruptedException ex)
        {
            System.out.println("Waiting interrupted.");
        }
        final SshClient sshClient = instanceGroup.createSshClient("ec2-user", PregelAuthenticator.getMasterPrivateKey());
        File distjar = new File("dist/" + JARNAME);
        if (distjar.exists())
        {
            try
            {
                sshClient.uploadFile(distjar, "~/" + JARNAME);
            } catch (IOException ex)
            {
                System.err.println("Error uploading distjar");
                System.exit(1);
            }
        } else
        {
            System.err.println("Didn't find jar in " + distjar.getAbsolutePath());
        }
        try
        {
            sshClient.uploadFile(jars, "~/jars.tar");
            sshClient.uploadFile(privateKeyFile, "~/" + privateKeyFile.getName());
            sshClient.executeCommand("tar -zxvf jars.tar", null);
        } catch (IOException ex)
        {
            System.out.println("Unable to upload file." + ex.getLocalizedMessage());
            System.exit(1);
        }
        new Thread(new Runnable()
        {

            @Override
            public void run()
            {
                try
                {
                    sshClient.executeCommand("java -server -cp " + JARNAME + ":./dist/lib/*"
                            + " -Djava.security.policy=policy"
                            + heapsize
                            + " JpAws.Ec2Master "
                            + args.toString(), System.out);
                } catch (IOException ex)
                {
                    System.out.println("Master disconnected " + ex.getLocalizedMessage());
                }
            }
        }).start();
    }

    @Override
    File getKey()
    {
        return PregelAuthenticator.getMasterPrivateKey();
    }

    @Override
    public String getHostname()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
