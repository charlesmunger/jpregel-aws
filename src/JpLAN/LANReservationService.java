package JpLAN;

import api.Cluster;
import api.MachineGroup;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import system.ClientToMaster;
import system.ReservationServiceImpl;
import system.Worker;

/**
 *
 * @author charlesmunger
 */
public class LANReservationService extends ReservationServiceImpl {

    @Override
    public MachineGroup<Worker> callWorker(String instanceType, int numberOfWorkers)
    {
        return new LANWorkerMachineGroup(numberOfWorkers);
    }

    @Override
    public MachineGroup<ClientToMaster> callMaster(String instanceType)
    {
        return new LANMasterMachineGroup();
    }
    
    public static Cluster newLocalCluster(int numWorkers) throws InterruptedException, ExecutionException, IOException {
        LANReservationService rs = new LANReservationService();
        return new Cluster (rs,"","",numWorkers);
    }
}
