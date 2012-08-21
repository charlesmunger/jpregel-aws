package JpAws;

import api.MachineGroup;
import com.amazonaws.services.ec2.model.Instance;
import datameer.awstasks.aws.ec2.InstanceGroup;
import datameer.awstasks.aws.ec2.ssh.SshClient;
import java.io.File;
import java.io.IOException;
import java.util.List;

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
