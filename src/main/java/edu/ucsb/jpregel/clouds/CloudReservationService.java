/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsb.jpregel.clouds;

import api.MachineGroup;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.name.Named;
import edu.ucsb.jpregel.system.ClientToMaster;
import edu.ucsb.jpregel.system.FileSystem;
import edu.ucsb.jpregel.system.ReservationServiceImpl;
import edu.ucsb.jpregel.system.Worker;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jicosfoundation.Service;
import org.jclouds.ContextBuilder;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.aws.ec2.reference.AWSEC2Constants;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.compute.options.TemplateOptions.Builder;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.scriptbuilder.domain.Statement;
import org.jclouds.scriptbuilder.statements.login.AdminAccess;
import org.jclouds.sshj.config.SshjSshClientModule;

/**
 *
 * @author Charles
 */
public class CloudReservationService extends ReservationServiceImpl {

    private final ComputeService context;
    public static final String SECURITY_GROUP = "jpregelgroup";
    private final String compute;
    private final ApiMetadata storage;

    @Inject
    public CloudReservationService(
            @Named("compute") String compute,
            @Named("storage") ApiMetadata storage,
            @Named("cUser") String cUser,
            @Named("cPass") String cPass) {
        this.compute = compute;
        this.storage = storage;
        Properties properties = new Properties();
        properties.setProperty(AWSEC2Constants.PROPERTY_EC2_AMI_QUERY, "owner-id=137112412989;state=available;image-type=machine");
        properties.setProperty(AWSEC2Constants.PROPERTY_EC2_CC_AMI_QUERY, "");
        Iterable<Module> modules = ImmutableSet.<Module>of(new SLF4JLoggingModule(),new SshjSshClientModule());
        context = ContextBuilder.newBuilder(compute)
                .credentials(cUser, cPass)
                .overrides(properties)
                .modules(modules)
                .build(ComputeServiceContext.class)
                .getComputeService();
        System.out.println(context);
    }

    @Override
    public MachineGroup<Worker> callWorker(String instanceType, int numberOfWorkers) {
        try {
            Set<? extends NodeMetadata> reserveNodes = reserveNodes(instanceType, numberOfWorkers);
            return new CloudMachineGroup<Worker>(reserveNodes, context) {
                
                void main(String[] args) throws Exception {
                    Module m = (Module) new ObjectInputStream(new FileInputStream("credentialsModule"));
                    Worker worker = new CloudWorker(Worker.getMaster(args[0]), m);
                    worker.init();
                }

                @Override
                protected Worker getRemoteReference() {
                    return null; //Workers don't need remote references
                }

                class CloudWorker extends Worker {

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
            };
        } catch (RunNodesException ex) {
            Logger.getLogger(CloudReservationService.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public MachineGroup<ClientToMaster> callMaster(String instanceType) {
        try {
            Set<? extends NodeMetadata> createNodesInGroup = reserveNodes(instanceType, 1);
            return new CloudMasterMachineGroup(createNodesInGroup, context);
        } catch (RunNodesException ex) {
            Logger.getLogger(CloudReservationService.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private Set<? extends NodeMetadata> reserveNodes(String instanceType, int count) throws RunNodesException {
        System.out.printf(">> adding node to group %s%n", SECURITY_GROUP);
        TemplateBuilder templateBuilder = context.templateBuilder();
        Statement bootInstructions = AdminAccess.standard();
        Template build = templateBuilder.options(Builder.runScript(bootInstructions))
                .hardwareId(instanceType)
                .build();
        System.out.println(build.getOptions().getPublicKey());
        System.out.println(build.getOptions().getPrivateKey());
        Set<? extends NodeMetadata> createNodesInGroup = context
                .createNodesInGroup(SECURITY_GROUP, count, build);
        return createNodesInGroup;
    }
}
