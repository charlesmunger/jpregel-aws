package clients;
import edu.ucsb.jpregel.system.VertexSources;
import edu.ucsb.jpregel.system.WorkerOutputMakerStandard;
import edu.ucsb.jpregel.system.MasterOutputMakerStandard;
import edu.ucsb.jpregel.system.ClientToMaster;
import edu.ucsb.jpregel.system.MasterGraphMakerStandard;
import edu.ucsb.jpregel.system.WorkerGraphMakerStandard;
import edu.ucsb.jpregel.system.Job;
import edu.ucsb.jpregel.system.LocalReservationService;

public class SourcesLocalClient
{
    /**
     * @param args [0]: Job directory name
     */
    public static void main( String[] args ) throws Exception
    {
        Job job = new Job(
                "Identify source nodes",  // jobName
                args[0],                           // jobDirectoryName
                new VertexSources(),     // vertexFactory
                new MasterGraphMakerStandard(),  
                new WorkerGraphMakerStandard(),   
                new MasterOutputMakerStandard(),
                new WorkerOutputMakerStandard()                 
                );
        int numWorkers = 1; 
        System.out.println( job + "\n    numWorkers: " + numWorkers );
        ClientToMaster master = LocalReservationService.newCluster( numWorkers );
        System.out.println( master.run( job ) );
        System.exit( 0 );
    }
}