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
        final AWSModule awsModule = new AWSModule("AKIAJUS57AJP3HWCSPAA", "rjqAEE9+D72Z0ixMxgvoJ60LB6LDDM0LpOls2shd");
        new ObjectOutputStream(new FileOutputStream(new File("credentialsModule"))).writeObject(awsModule);
        Injector injector = Guice.createInjector(awsModule);
        ReservationService instance = injector.getInstance(ReservationService.class);
        Future<MachineGroup<ClientToMaster>> reserveMaster = instance.reserveMaster(InstanceType.M1_SMALL);
        Future<MachineGroup<Worker>> reserveWorkers = instance.reserveWorkers(InstanceType.M1_SMALL, numWorkers);
        Future<ClientToMaster> master = reserveMaster.get().deploy("");
        reserveWorkers.get().deploy(reserveMaster.get().getHostname());
        Job job3 = new Job("Binary Tree Shortest Path three", // jobName
                args[0] + "3", // jobDirectoryName
                new VertexShortestPathBinaryTree(), // vertexFactory
                new MasterGraphMakerBinaryTree(),
                new WorkerGraphMakerBinaryTree(),
                new MasterOutputMakerStandard(),
                new WorkerOutputMakerStandard());
        JobRunData run3 = master.get().run(job3);
      	System.out.println(run3);
//		master.terminate();
//		System.exit(0);
    }
}
