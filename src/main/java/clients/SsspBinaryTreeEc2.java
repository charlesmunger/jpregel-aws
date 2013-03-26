package clients;

import api.MachineGroup;
import api.ReservationService;
import com.google.inject.Guice;
import com.google.inject.Injector;
import edu.ucsb.jpregel.clouds.CloudMachineGroup;
import edu.ucsb.jpregel.clouds.modules.AWSModule;
import edu.ucsb.jpregel.system.ClientToMaster;
import edu.ucsb.jpregel.system.Job;
import edu.ucsb.jpregel.system.JobRunData;
import edu.ucsb.jpregel.system.MasterGraphMakerBinaryTree2;
import edu.ucsb.jpregel.system.MasterOutputMakerStandard;
import edu.ucsb.jpregel.system.Worker;
import edu.ucsb.jpregel.system.WorkerGraphMakerBinaryTree2;
import edu.ucsb.jpregel.system.WorkerOutputMakerStandard;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.Future;
import org.jclouds.ec2.domain.InstanceType;
import vertices.VertexShortestPathBinaryTree;

/**
 * 
 * @param args [0] directory name on S3
 *             [1] numbers of worker machines
 *             [2] access key
 *             [3] secret key
 * @author Charles
 */
public class SsspBinaryTreeEc2 
{
    public static void main( String[] args ) throws Exception 
    {
        int numWorkers = Integer.parseInt(args[1]);
        final AWSModule awsModule = new AWSModule(args[2], args[3]);
        new ObjectOutputStream(new FileOutputStream(new File(CloudMachineGroup.CREDENTIALS_MODULE))).writeObject(awsModule);
        Injector injector = Guice.createInjector(awsModule);
        ReservationService instance = injector.getInstance(ReservationService.class);
        Future<MachineGroup<ClientToMaster>> reserveMaster = instance.reserveMaster(InstanceType.M1_SMALL);
        Future<MachineGroup<Worker>> reserveWorkers = instance.reserveWorkers(InstanceType.M1_SMALL, numWorkers);
        Future<ClientToMaster> master = reserveMaster.get().deploy(args[1]);
        reserveWorkers.get().deploy(reserveMaster.get().getHostname());
        final ClientToMaster rMaster = master.get();

        Job job3 = new Job("Binary Tree Shortest Path", // jobName
                args[0], // jobDirectoryName
                new VertexShortestPathBinaryTree(), // vertexFactory
                new MasterGraphMakerBinaryTree2(),
                new WorkerGraphMakerBinaryTree2(),
                new MasterOutputMakerStandard(),
                new WorkerOutputMakerStandard());

        JobRunData run3 = rMaster.run(job3);
        System.out.println(run3);
        reserveMaster.get().terminate();
        reserveWorkers.get().terminate();
        System.exit(0);
    }
}
