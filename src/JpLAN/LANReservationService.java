package JpLAN;

import api.MachineGroup;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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
    
    public static ClientToMaster newLocalCluster(int numWorkers) throws InterruptedException, ExecutionException, IOException {
        LANReservationService rs = new LANReservationService();
        Future<MachineGroup<ClientToMaster>> masterMachine = rs.reserveMaster("m1.small");
        Future<MachineGroup<Worker>> workers = rs.reserveWorkers("m1.small", numWorkers);
        Future<ClientToMaster> deployMaster = masterMachine.get().deploy(Integer.toString(numWorkers));
        workers.get().deploy(masterMachine.get().getHostname());
        System.out.println("Cluster created with "+ numWorkers +" workers");
        return deployMaster.get();
    }
}
