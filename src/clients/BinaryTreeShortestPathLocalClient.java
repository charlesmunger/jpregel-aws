package clients;

import system.*;

/**
 *
 * @author Pete Cappello
 */
public class BinaryTreeShortestPathLocalClient 
{  
    /**
     * @param args [0]: Job directory name
     */
    public static void main( String[] args ) throws Exception
    {
        int numWorkers = Integer.parseInt(args[1]);  
        Job job = new Job("Binary Tree Shortest Path",        // jobName
                args[0],                            // jobDirectoryName
                new VertexShortestPathBinaryTree(), // vertexFactory
                new MasterGraphMakerBinaryTree(),  
                new WorkerGraphMakerBinaryTree(),   
                new MasterOutputMakerStandard(),
                new WorkerOutputMakerStandard()                 
                );
        System.out.println( job + "\n    numWorkers: " + numWorkers );
        ClientToMaster master = LocalReservationService.newCluster(numWorkers);
        System.out.println(master.run(job));
        
//        job = new Job("Shortest Path Problem", // jobName
//                  "examples/ShortestPath",                         // jobDirectoryName
//                  new VertexShortestPath(),        // vertexFactory, 
//                  new MasterGraphMakerG1(),
//                  new WorkerGraphMakerStandard(),
//                  new MasterOutputMakerStandard(),
//                  new WorkerOutputMakerStandard(),
//                  new AggregatorSumInteger(),   // problem aggregator
//                  new AggregatorSumInteger()    // step    agregator
//                );
        System.out.println( job );
        System.out.println( master.run( job ) );
        System.exit( 0 );
    }
}
