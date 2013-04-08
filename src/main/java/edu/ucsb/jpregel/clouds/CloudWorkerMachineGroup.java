package edu.ucsb.jpregel.clouds;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.name.Named;
import edu.ucsb.jpregel.system.FileSystem;
import edu.ucsb.jpregel.system.Worker;
import java.rmi.RemoteException;
import java.util.Set;
import jicosfoundation.Service;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.NodeMetadata;

/**
 *
 * @author charlesmunger
 */
public class CloudWorkerMachineGroup extends CloudMachineGroup<Worker> {

    public CloudWorkerMachineGroup(Set<? extends NodeMetadata> nodes, ComputeService compute, @Named("storage") ApiMetadata ap) {
        super(nodes, compute, ap);
    }

    public static void main(String[] args) throws Exception {
        Worker worker = new CloudWorker(Worker.getMaster(args[0]), getCredentials());
        worker.init();
    }

    @Override
    protected Worker getRemoteReference() {
        return null; //Workers don't need remote references
    }

    static class CloudWorker extends Worker {

        private final Module credentialsModule;

        @Inject
        public CloudWorker(Service master, Module credentialsModule) throws RemoteException {
            super(master);
            this.credentialsModule = credentialsModule;
        }

        @Override
        public FileSystem makeFileSystem(final String jobDirectoryName) {
            return Guice.createInjector(credentialsModule, CloudFileSystem.getModule(jobDirectoryName)).getInstance(FileSystem.class);
        }
    }

}
