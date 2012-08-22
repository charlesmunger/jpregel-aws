package JpAws;

import datameer.awstasks.aws.ec2.InstanceGroup;
import datameer.awstasks.aws.ec2.ssh.SshClient;
import java.io.File;
import java.io.IOException;
import system.Worker;

/**
 *
 * @author charlesmunger
 */
public class Ec2WorkerMachineGroup extends Ec2MachineGroup<Worker>
{

    public static final String JARNAME = "jpregel-aws.jar";
    private final String hostName = null;

    public Ec2WorkerMachineGroup(InstanceGroup i, String heapsize)
    {
        super(i, heapsize);
    }

    @Override
    File getKey()
    {
        return PregelAuthenticator.getPrivateKey();
    }

    @Override
    public String getHostname()
    {
        return hostName;
    }

    @Override
    public Worker syncDeploy(final String... args)
    {
        File privateKeyFile = PregelAuthenticator.getPrivateKey();
        File jars = new File("jars.tar");

        final SshClient sshClient = instanceGroup.createSshClient("ec2-user", privateKeyFile, false);
        File thisjar = new File(JARNAME);
        File distjar = new File("dist/" + JARNAME);
        if (distjar.exists())
        {
            try
            {
                sshClient.uploadFile(distjar, "~/" + JARNAME);
            } catch (IOException ex)
            {
                System.out.println("Error uploading distjar");
                System.exit(1);
            }
        } else
        {
            System.err.println("Didn't find jar in " + distjar.getAbsolutePath() + " or " + thisjar.getAbsolutePath());
            System.exit(1);
        }
        try
        {
            sshClient.uploadFile(jars, "~/jars.tar");
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
                            + " -Djava.security.policy=policy "
                            + heapsize
                            + " JpAws.Ec2Worker " + args[0], System.out);
                } catch (IOException ex)
                {
                    System.out.println("Workers terminated");
                }
            }
        }).start();
        return null;
    }
}
