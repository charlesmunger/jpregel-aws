package JpAws;

import datameer.awstasks.aws.ec2.InstanceGroup;
import datameer.awstasks.aws.ec2.ssh.SshClient;
import java.io.File;
import java.io.IOException;
import java.rmi.Naming;
import system.ClientToMaster;
import system.Master;

/**
 *
 * @author charlesmunger
 */
public class Ec2MasterMachineGroup extends Ec2MachineGroup<ClientToMaster>
{

    public static final String JARNAME = "jpregel-aws.jar";
    private final String hostName;

    public Ec2MasterMachineGroup(InstanceGroup i, String heapsize)
    {
        super(i, heapsize);
        hostName = i.getInstances(true).get(0).getPrivateDnsName();
    }

    void startObject(final String[] args)
    {
        File jars = new File("jars.tar");
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
                            + " JpAws.Ec2Master "
                            + args[0], System.out);
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
        return hostName;
    }

    private void tryAgain(int i)
    {
        System.out.println("Master not up yet. Trying again in 5 seconds...");
        try
        {
            Thread.sleep(5000);
        } catch (InterruptedException ex1)
        {
            System.out.println("Waiting interrupted, trying again immediately");
        }
    }

    @Override
    public ClientToMaster syncDeploy(String... args)
    {
        startObject(args);
        ClientToMaster remoteObject = null;
        String url = "//" + getHostname() + ":" + Master.PORT + "/" + Master.CLIENT_SERVICE_NAME;
        for (int i = 0;; i += 5000)
        {
            try
            {
                remoteObject = (ClientToMaster) Naming.lookup(url);
            } catch (Exception ex)
            {
                tryAgain(i);
                continue;
            }
            break;
        }
        return remoteObject;
    }
}
