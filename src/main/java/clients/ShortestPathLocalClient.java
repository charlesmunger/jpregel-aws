package clients;

import edu.ucsb.jpregel.system.MasterGraphMakerG1;
import edu.ucsb.jpregel.system.WorkerOutputMakerStandard;
import edu.ucsb.jpregel.system.MasterOutputMakerStandard;
import edu.ucsb.jpregel.system.AggregatorSumInteger;
import edu.ucsb.jpregel.system.ClientToMaster;
import edu.ucsb.jpregel.system.WorkerGraphMakerStandard;
import edu.ucsb.jpregel.system.Job;
import edu.ucsb.jpregel.system.LocalReservationService;
import edu.ucsb.jpregel.system.VertexShortestPath;

/**
 *
 * @author Pete Cappello
 */
public class ShortestPathLocalClient 
{
    /**
     * @param args [0]: Job Directory
     */
    public static void main(String[] args) throws Exception 
    {
        int numWorkers = Integer.parseInt( args[1] );
        Job job = new Job("Shortest Path Problem", // jobName
                  args[0],                         // jobDirectoryName
                  new VertexShortestPath(),        // vertexFactory, 
                  new MasterGraphMakerG1(),
                  new WorkerGraphMakerStandard(),
                  new MasterOutputMakerStandard(),
                  new WorkerOutputMakerStandard(),
                  new AggregatorSumInteger(),   // problem aggregator
                  new AggregatorSumInteger()    // step    agregator
                );
        System.out.println(job + "\n         numWorkers: " + numWorkers );
        ClientToMaster master = LocalReservationService.newCluster(numWorkers);
        System.out.println(master.run(job));
        System.exit(0);
    }
}
