/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsb.jpregel.clouds;

import api.MachineGroup;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Module;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.RunScriptOnNodesException;
import org.jclouds.compute.domain.ExecChannel;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.io.Payloads;
import org.jclouds.ssh.SshClient;

/**
 *
 * @author Charles Munger
 */
public abstract class CloudMachineGroup<T> extends MachineGroup<T> {

    protected final Set<? extends NodeMetadata> nodes;
    protected final ComputeService comp;
    public static final String JARNAME = "jpregel.jar";
    private static final File jar = new File("target/" + JARNAME);
    public static final String CREDENTIALS_MODULE = "credentialsModule";

    @Inject
    CloudMachineGroup(Set<? extends NodeMetadata> nodes, ComputeService compute) {
        this.nodes = nodes;
        this.comp = compute;
    }

    @Override
    public String getHostname() {
        return Iterables.getOnlyElement(
                Iterables.getOnlyElement(
                nodes).getPrivateAddresses());
    }
    
    protected String getPublicHostname() {
        return Iterables.getOnlyElement(
                Iterables.getOnlyElement(
                nodes).getPublicAddresses());
    }

    @Override
    public void reset() throws IOException {
        try {
            comp.runScriptOnNodesMatching(new Predicate<NodeMetadata>() {
                public boolean apply(NodeMetadata t) {
                    return nodes.contains(t);
                }
            }, "killall -9 java; cd; rm -rf *");
        } catch (RunScriptOnNodesException ex) {
            Logger.getLogger(CloudMachineGroup.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void terminate() throws IOException {
        comp.destroyNodesMatching(new Predicate<NodeMetadata>() {
            public boolean apply(NodeMetadata t) {
                return nodes.contains(t);
            }
        });
    }

    @Override
    public final T syncDeploy(String... args) {
        try {
            System.out.println("syncDeploying");
            ExecutorService e = Executors.newCachedThreadPool();
            for (NodeMetadata n : nodes) {
                e.submit(new LaunchTask(n, this.getClass().getName(), args));
            }
            e.shutdown();
            e.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (Exception ex) {
            Logger.getLogger(CloudMachineGroup.class.getName()).log(Level.SEVERE, null, ex);
        }
        return getRemoteReference();
    }

    protected abstract T getRemoteReference();

    private class LaunchTask implements Runnable {
        private final String mainClass;
        private final NodeMetadata nm;
        private final String[] args;

        LaunchTask(NodeMetadata nm, String mainClass, String[] args) {
            this.nm = nm;
            this.mainClass = mainClass;
            this.args = args;
        }

        @Override
        public void run() {
            System.out.println("Connecting");
            SshClient ssh = comp.getContext().utils().sshForNode().apply(nm);
            System.out.println("Got client " + ssh.getUsername() + "@" + ssh.getHostAddress());
            try {
                ssh.connect();
                System.out.println("Connected");
                ssh.exec("echo \"grant{ permission java.security.AllPermission;};\" > ~/policy ");
                System.out.println("Policy uploaded");
                ssh.put(CREDENTIALS_MODULE, Payloads.newFilePayload(new File(CREDENTIALS_MODULE)));
                ssh.put(JARNAME, Payloads.newPayload(jar));
                System.out.println("Jar uploaded");
                ssh.execChannel("java -server "
                        + "-XX:+UseConcMarkSweepGC "
                        + "-XX:+AggressiveOpts "
                        + "-XX:+UseFastAccessorMethods "
                        + "-cp " + JARNAME
                        + " -Djava.security.policy=policy "
                        //ssh address so we know it's an address the client can reach
                        + " -Djava.rmi.server.hostname=" + ssh.getHostAddress() + " " 
                        + heapSize(nm.getHardware().getRam())
                        + " " + mainClass + " "
                        + args[0]
                        + " >> log.log"); //save output until a real logger works
                System.out.println(ssh.getHostAddress() + " Executions submitted");
            } catch (Exception ex) {
                Logger.getLogger(CloudMachineGroup.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (ssh != null) {
                    ssh.disconnect();
                }
            }
        }
    }

    protected static Module getCredentials() throws IOException, ClassNotFoundException {
        ObjectInputStream oi = new ObjectInputStream(new FileInputStream(CREDENTIALS_MODULE));
        Module m = (Module) oi.readObject();
        oi.close();
        return m;
    }

    private String heapSize(int ram) {
        ram = (ram * 19) / 20;
        return "-Xmx" + ram + "m -Xms" + ram + "m ";
    }
}
