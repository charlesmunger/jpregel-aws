/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clients;

import api.MachineGroup;
import api.ReservationService;
import com.google.inject.Guice;
import com.google.inject.Injector;
import edu.ucsb.jpregel.clouds.modules.AWSModule;
import edu.ucsb.jpregel.system.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.Future;
import org.jclouds.ec2.domain.InstanceType;
import vertices.VertexShortestPathBinaryTree;

/**
 *
 * @author Charles
 */
public class BinaryTreeEc2Client {

    public static void main(String[] args) throws Exception {
        int numWorkers = Integer.parseInt(args[1]);
        final AWSModule awsModule = new AWSModule(args[2], args[3]);
        new ObjectOutputStream(new FileOutputStream(new File("credentialsModule"))).writeObject(awsModule);
        Injector injector = Guice.createInjector(awsModule);
        ReservationService instance = injector.getInstance(ReservationService.class);
        Future<MachineGroup<ClientToMaster>> reserveMaster = instance.reserveMaster(InstanceType.M1_SMALL);
        Future<MachineGroup<Worker>> reserveWorkers = instance.reserveWorkers(InstanceType.M1_SMALL, numWorkers);
        Future<ClientToMaster> master = reserveMaster.get().deploy(args[1]);
        reserveWorkers.get().deploy(reserveMaster.get().getHostname());
        Job job3 = new Job("Binary Tree Shortest Path", // jobName
                args[0], // jobDirectoryName
                new VertexShortestPathBinaryTree(), // vertexFactory
                new MasterGraphMakerBinaryTree(),
                new WorkerGraphMakerBinaryTree(),
                new MasterOutputMakerStandard(),
                new WorkerOutputMakerStandard());
        JobRunData run3 = master.get().run(job3);
        System.out.println(run3);
        reserveMaster.get().terminate();
        reserveWorkers.get().terminate();
        System.exit(0);
    }
}
