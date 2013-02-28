/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsb.jpregel.clouds;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Module;
import edu.ucsb.jpregel.system.ClientToMaster;
import edu.ucsb.jpregel.system.FileSystem;
import edu.ucsb.jpregel.system.Master;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Set;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.NodeMetadata;

/**
 *
 * @author Charles
 */
class CloudMasterMachineGroup extends CloudMachineGroup<ClientToMaster> {

    public static void main(String[] args) throws Exception {
        System.setSecurityManager(new RMISecurityManager());
        Registry registry = LocateRegistry.createRegistry(Master.PORT);
        ClientToMaster master = new CloudMaster(getCredentials());
        registry.bind(Master.SERVICE_NAME, master);
        master.init(args.length > 0 ? Integer.parseInt(args[0]):0);
        registry.bind(Master.CLIENT_SERVICE_NAME, master);
        System.out.println("Master ready");
    }

    public CloudMasterMachineGroup(Set<? extends NodeMetadata> nodes, ComputeService cs) {
        super(nodes, cs);
    }
    
    @Override
    protected ClientToMaster getRemoteReference() {
        ClientToMaster remoteObject = null;
        String url = "//" + getPublicHostname() + ":" + Master.PORT + "/" + Master.CLIENT_SERVICE_NAME;
        for (int i = 0;; i += 5000) {
            try {
                remoteObject = (ClientToMaster) Naming.lookup(url);
            } catch (Exception ex) {
                System.out.println("Master "+url +" not up yet. Trying again in 5 seconds..."+ ex.getMessage());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex1) {
                    System.out.println("Waiting interrupted, trying again immediately");
                }
                continue;
            }
            break;
        }
        return remoteObject;
    }

    public static class CloudMaster extends Master {

        private final Module credentialsModule;

        @Inject
        public CloudMaster(Module credentialsModule) throws RemoteException {
            this.credentialsModule = credentialsModule;
        }

        @Override
        public FileSystem makeFileSystem(final String jobDirectoryName) {
            return Guice.createInjector(credentialsModule, CloudFileSystem.getModule(jobDirectoryName)).getInstance(FileSystem.class);
        }
    }
}
