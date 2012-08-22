package system;

/**
 *
 * @author Pete Cappello
 */
public class TestMutabilityClient 
{
    /**
     * @param args the command line arguments
     */
    public static void main( String[] args ) throws Exception
    {
        int computeThreadsPerWorker = Runtime.getRuntime().availableProcessors();
        int numWorkers = Integer.parseInt( args[1] );
        int numParts = numWorkers * computeThreadsPerWorker * 2; 
        Job job = new Job( 
                "Test Graph Mutability Features", 
                args[0],      // jobDirectoryName, 
                new VertexTestMutability(), 
                numParts,
                new MasterGraphMakerStandard(),
                new WorkerGraphMakerStandard(),
                new MasterOutputMakerStandard(),
                new WorkerOutputMakerStandard()
                );
        System.out.println( job + "\n        numWorkers: " + numWorkers );
        ClientToMaster master = LocalReservationService.newLocalCluster(numWorkers);
        System.out.println(master.run(job));
        System.exit( 0 );
    }
}
