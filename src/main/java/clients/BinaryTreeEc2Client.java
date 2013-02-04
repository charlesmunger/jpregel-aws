/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clients;

import com.google.inject.Guice;
import com.google.inject.Injector;
import edu.ucsb.jpregel.clouds.modules.AWSModule;
import edu.ucsb.jpregel.system.*;
import vertices.VertexShortestPathBinaryTree;

/**
 *
 * @author Charles
 */
public class BinaryTreeEc2Client {

	public static void main(String[] args) throws Exception {
		int numWorkers = Integer.parseInt(args[1]);
		Injector injector = Guice.createInjector(new AWSModule());
		ReservationServiceImpl instance = injector.getInstance(ReservationServiceImpl.class);


		if (args.length > 2) {
//            new AmazonS3Client(PregelAuthenticator.get()).putObject(args[0], "input", new File(args[2]));
		}
		Job job3 = new Job("Binary Tree Shortest Path three", // jobName
			args[0] + "3", // jobDirectoryName
			new VertexShortestPathBinaryTree(), // vertexFactory
			new MasterGraphMakerBinaryTree(),
			new WorkerGraphMakerBinaryTree(),
			new MasterOutputMakerStandard(),
			new WorkerOutputMakerStandard());
//		JobRunData run3 = master.run(job3);
//        System.out.println(run1);
//        System.out.println(run2);
//		System.out.println(run3);
//		master.terminate();
//		System.exit(0);
	}
}
