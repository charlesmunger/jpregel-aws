package JpAws;

import api.MachineGroup;
import com.amazonaws.services.ec2.model.Instance;
import datameer.awstasks.aws.ec2.InstanceGroup;
import datameer.awstasks.aws.ec2.ssh.SshClient;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import system.Master;

/**
 *
 * @author charlesmunger
 */
 abstract class Ec2MachineGroup<T> implements MachineGroup<T>
{

    private final String[] hostNames;
    protected final InstanceGroup instanceGroup;
    protected final String heapsize;
    public Ec2MachineGroup(InstanceGroup i, String heapsize)
    {
        instanceGroup = i;
        List<Instance> instances = (List<Instance>) i.getInstances(true);
        hostNames = new String[instances.size()];
        int count = 0;
        for (Instance instance : instances)
        {
            hostNames[count++] = instance.getPublicDnsName();
        }
        this.heapsize = heapsize;
    }

    @Override
    public T deploy(String... args)
    {
        startObject(args);
        T remoteObject = null;
        String url = "//" + getHostname() + ":" + Master.PORT + "/" + Master.SERVICE_NAME;
        for (int i = 0;; i += 5000)
        {
            try
            {
                remoteObject = (T) Naming.lookup(url);
            } catch (NotBoundException ex)
            {
                if (i > 120000)
                {
                }
                System.out.println("Master not up yet. Trying again in 5 seconds...");
                try
                {
                    wait(5000);
                } catch (InterruptedException ex1)
                {
                    System.out.println("Waiting interrupted, trying again immediately");
                }
                continue;
            } catch (RemoteException r)
            {
                if (i > 120000)
                {
                    System.out.println("Master not up in time. Aborting");
                }
                System.out.println("Master not up yet. Trying again in 5 seconds...");
                try
                {
                    wait(5000);
                } catch (InterruptedException ex1)
                {
                    System.out.println("Waiting interrupted, trying again immediately");
                }
                continue;
            } catch (MalformedURLException ex)
            {
            }
            break;
        }
        return remoteObject;
    }
    
    @Override
    public void reset() throws IOException{
        SshClient ssh = instanceGroup.createSshClient("ec2-user",getKey());
        ssh.executeCommand("killall -9 java; cd; rm -rf *", null);
    }
    
    @Override
    public void terminate() {
        instanceGroup.terminate();
    }

    abstract void startObject(String[] args);
    
    abstract File getKey();
}
