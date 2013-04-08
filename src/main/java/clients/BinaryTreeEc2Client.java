package clients;

import api.MachineGroup;
import api.ReservationService;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import edu.ucsb.jpregel.clouds.CloudMachineGroup;
import edu.ucsb.jpregel.clouds.CloudReservationService;
import edu.ucsb.jpregel.clouds.modules.AWSModule;
import edu.ucsb.jpregel.system.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.Future;
import org.jclouds.ContextBuilder;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.blobstore.AsyncBlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.ec2.domain.InstanceType;
import org.jclouds.blobstore.options.PutOptions;
import vertices.VertexShortestPathBinaryTree;

/**
 * Problem: SSSP; Graph family: binary tree; Cloud: AWS.
 * @param args [0] directory name on S3
 *             [1] numbers of worker machines
 *             [2] access key
 *             [3] secret key
 * @author Charles
 */
public class BinaryTreeEc2Client
{
    public static void main( String[] args ) throws Exception 
    {
        int numWorkers = Integer.parseInt(args[1]);
        final AWSModule awsModule = new AWSModule(args[2], args[3]);
        new ObjectOutputStream(
                new FileOutputStream(
                new File(CloudMachineGroup.CREDENTIALS_MODULE))).writeObject(awsModule);
        
        Injector injector = Guice.createInjector(awsModule);
        CloudReservationService instance = (CloudReservationService)injector.getInstance(ReservationService.class);
        AsyncBlobStore context = new ContextBuilder(
                injector.getInstance(Key.get(ApiMetadata.class, Names.named("storage"))))
                .credentials(
                injector.getInstance(Key.get(String.class, Names.named("sAccess"))),
                injector.getInstance(Key.get(String.class, Names.named("sModify"))))
                .build(BlobStoreContext.class)
                .getAsyncBlobStore();
        
        Blob build = context.blobBuilder(CloudMachineGroup.JARNAME).payload(CloudMachineGroup.jar).build();
        System.out.println("Uploading jar to storage");
        ListenableFuture<String> putBlob = context.putBlob(CloudMachineGroup.BUCKET_NAME, build, PutOptions.Builder.multipart());
        Future<MachineGroup[]> m = instance.reserveBoth(InstanceType.CC2_8XLARGE, numWorkers);
        System.out.println("Blob submitted" + putBlob.get());
        Future<ClientToMaster> frm = (Future<ClientToMaster>) m.get()[0].deploy(args[1]); //master
        m.get()[1].deploy(m.get()[0].getHostname()).get();
        ClientToMaster rMaster = frm.get();
        Job job3 = new Job("Binary Tree Shortest Path", // jobName
                args[0], 
                new VertexShortestPathBinaryTree(), // vertexFactory
                new MasterGraphMakerBinaryTree(),
                new WorkerGraphMakerBinaryTree(),
                new MasterOutputMakerStandard(),
                new WorkerOutputMakerStandard());

        for (int j = 0; j < 3; j++) {
            try {
                JobRunData run3 = rMaster.run(job3);
                System.out.println(run3);
            } catch (Exception e) {
                System.out.println("Exception running job");
                System.out.println(e.getLocalizedMessage());
                e.printStackTrace(System.out);
                continue;
            }
            break;
        }

        System.exit(0);
    }
}
