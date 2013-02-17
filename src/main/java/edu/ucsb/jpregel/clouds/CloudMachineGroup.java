/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsb.jpregel.clouds;

import api.MachineGroup;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
    public static final String JARNAME = "jpregel-aws.jar";
    private final File jar = new File("dist/" + JARNAME);

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
        System.out.println("syncDeploying");
        ExecutorService e = Executors.newCachedThreadPool();
        for (NodeMetadata n : nodes) {
            e.submit(new LaunchTask(n, this.getClass().getName(), args));
        }
        try {
            e.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
            Logger.getLogger(CloudMachineGroup.class.getName()).log(Level.SEVERE, null, ex);
        }
        e.shutdown();
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
            try {
                ssh.connect();
                System.out.println("Connected");
                ssh.exec("echo \"grant{ permission java.security.AllPermission;>;\" > ~/policy ");
                ssh.put("~/credentialsModule", Payloads.newFilePayload(new File("credentialsModule")));
                ssh.put("~/" + JARNAME, Payloads.newPayload(jar));
                ExecChannel execChannel = ssh.execChannel("java -server -cp " + JARNAME
                        + " -Djava.security.policy=policy "
                        + heapSize(nm.getHardware().getRam())
                        + " " + mainClass + " "
                        + args[0]);
                Thread.sleep(10000);
                BufferedReader br = new BufferedReader(new InputStreamReader(execChannel.getOutput()));
                String s;
                System.out.println("Deployed");
                while ((s = br.readLine()) != null) {
                    System.out.println(s);
                }
                br.close();
            } catch (Exception ex) {
                Logger.getLogger(CloudMachineGroup.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (ssh != null) {
                    ssh.disconnect();
                }
            }
        }
    }

    private String heapSize(int ram) {
        ram = (ram * 19) / 20;
        return "-Xmx" + ram + "m -Xms" + ram + "m ";
    }
}
