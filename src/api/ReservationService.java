package api;

import java.util.concurrent.Future;
import system.ClientToMaster;
import system.Worker;

/**
 *
 * @author charlesmunger
 */
public interface ReservationService
{
    public Future<MachineGroup<Worker>> reserveWorkers(String instanceType, int numberOfWorkers);
    public Future<MachineGroup<ClientToMaster>> reserveMaster(String instanceType);
}
