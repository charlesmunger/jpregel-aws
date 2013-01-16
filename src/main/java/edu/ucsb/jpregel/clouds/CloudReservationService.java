/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsb.jpregel.clouds;

import api.MachineGroup;
import edu.ucsb.jpregel.system.ClientToMaster;
import edu.ucsb.jpregel.system.ReservationServiceImpl;
import edu.ucsb.jpregel.system.Worker;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jclouds.ContextBuilder;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.compute.options.TemplateOptions.Builder;
import org.jclouds.scriptbuilder.domain.Statement;
import org.jclouds.scriptbuilder.statements.login.AdminAccess;

/**
 *
 * @author Charles
 */
public class CloudReservationService extends ReservationServiceImpl {

    private final ComputeService context;
    public static final String SECURITY_GROUP = "jpregelgroup";
    private final ApiMetadata compute;
    private final ApiMetadata storage;

    public CloudReservationService(ApiMetadata apiMetadata,ApiMetadata storage) {
        this.compute = apiMetadata;
	this.storage = storage;
        context = new ContextBuilder(apiMetadata).credentials(null, null).build(ComputeServiceContext.class).getComputeService();
    }

    @Override
    public MachineGroup<Worker> callWorker(String instanceType, int numberOfWorkers) {
        try {
            System.out.printf(">> adding node to group %s%n", SECURITY_GROUP);
            // Default template chooses the smallest size on an operating system
            // that tested to work with java, which tends to be Ubuntu or CentOS
            TemplateBuilder templateBuilder = context.templateBuilder();

            // note this will create a user with the same name as you on the
            // node. ex. you can connect via ssh publicip
            Statement bootInstructions = AdminAccess.standard();

            // to run commands as root, we use the runScript option in the template.
            templateBuilder.options(Builder.runScript(bootInstructions));
            Set<? extends NodeMetadata> createNodesInGroup = context.createNodesInGroup(SECURITY_GROUP, 1, templateBuilder.build());
	    return null;
        } catch (RunNodesException ex) {
            Logger.getLogger(CloudReservationService.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public MachineGroup<ClientToMaster> callMaster(String instanceType) {
        try {
            System.out.printf(">> adding node to group %s%n", SECURITY_GROUP);
            // Default template chooses the smallest size on an operating system
            // that tested to work with java, which tends to be Ubuntu or CentOS
            TemplateBuilder templateBuilder = context.templateBuilder();

            // note this will create a user with the same name as you on the
            // node. ex. you can connect via ssh publicip
            Statement bootInstructions = AdminAccess.standard();

            // to run commands as root, we use the runScript option in the template.
            templateBuilder.options(Builder.runScript(bootInstructions));
            Set<? extends NodeMetadata> createNodesInGroup = context.createNodesInGroup(SECURITY_GROUP, 1, templateBuilder.build());
            return new CloudMasterMachineGroup(createNodesInGroup, compute);
        } catch (RunNodesException ex) {
            Logger.getLogger(CloudReservationService.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
