package edu.ucsb.jpregel.system;

import api.MachineGroup;
import api.ReservationService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author charlesmunger
 */
public abstract class ReservationServiceImpl implements ReservationService 
{
    private ExecutorService exec = Executors.newCachedThreadPool();
    
    @Override
    public Future<MachineGroup<Worker>> reserveWorkers(final String instanceType, final int numberOfWorkers)
    {
        return exec.submit(new Callable<MachineGroup<Worker>> () 
        {
            @Override
            public MachineGroup<Worker> call() throws Exception
            {
                return callWorker(instanceType, numberOfWorkers);
            }
        });
    }

    @Override
    public Future<MachineGroup<ClientToMaster>> reserveMaster(final String instanceType)
    {
        return exec.submit(new Callable<MachineGroup<ClientToMaster>> () 
        {
            @Override
            public MachineGroup<ClientToMaster> call() throws Exception
            {
                return callMaster(instanceType);
            }
        });
    }

    public abstract MachineGroup<Worker>callWorker(String instanceType, int numberOfWorkers);
    
    public abstract MachineGroup<ClientToMaster>callMaster(String instanceType);
}
