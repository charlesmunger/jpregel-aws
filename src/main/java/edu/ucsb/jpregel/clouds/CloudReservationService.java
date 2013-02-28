/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsb.jpregel.clouds;

import api.MachineGroup;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.name.Named;
import edu.ucsb.jpregel.system.ClientToMaster;
import edu.ucsb.jpregel.system.ReservationServiceImpl;
import edu.ucsb.jpregel.system.Worker;
import java.io.File;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jclouds.ContextBuilder;
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
    private final String username = System.getProperty("user.name");

    @Inject
    public CloudReservationService(
            @Named("compute") String compute,
            @Named("cUser") String cUser,
            @Named("cPass") String cPass) {
        Properties properties = new Properties();
        properties.setProperty(AWSEC2Constants.PROPERTY_EC2_AMI_QUERY, "owner-id=137112412989;state=available;image-type=machine");
        properties.setProperty(AWSEC2Constants.PROPERTY_EC2_CC_AMI_QUERY, "");
        Iterable<Module> modules = ImmutableSet.<Module>of(new SLF4JLoggingModule(), new SshjSshClientModule());
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
            return new CloudWorkerMachineGroup(reserveNodes, context);
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
        System.out.printf(">> adding nodes to group %s %n", SECURITY_GROUP,count);
        TemplateBuilder templateBuilder = context.templateBuilder();
        Statement bootInstructions = AdminAccess.builder()
                .adminPublicKey(new File("key.pub"))
                .adminPrivateKey(new File("key"))
                .authorizeAdminPublicKey(true)
                .adminUsername(username)
                .installAdminPrivateKey(true)
                .build();
        Template build = templateBuilder.options(Builder.runScript(bootInstructions))
                .hardwareId(instanceType)
                .build();
        System.out.println(build.getOptions().getPublicKey());
        System.out.println(build.getOptions().getPrivateKey());
        Set<? extends NodeMetadata> createNodesInGroup = context
                .createNodesInGroup(SECURITY_GROUP, count, build);
        System.out.println(createNodesInGroup.iterator().next().getCredentials().getPrivateKey());
        return createNodesInGroup;
    }
}
