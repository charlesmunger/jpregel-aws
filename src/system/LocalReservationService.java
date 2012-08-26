package system;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author charlesmunger
 */
public class LocalReservationService
{

    public static ClientToMaster newLocalCluster(final int numWorkers) throws Exception
    {
        final Master master = new LocalMaster();
        Worker[] workers = new Worker[numWorkers];

        Future<Master> submit = Executors.newSingleThreadExecutor().submit(new Callable<Master>()
        {

            @Override
            public Master call() throws Exception
            {
                master.init(numWorkers);
                return master;
            }
        });
        for (int workerNum = 0; workerNum < numWorkers; workerNum++)
        {
            workers[ workerNum] = new LocalWorker(master);
            workers[workerNum].init();
        }
        submit.get();
        return master;
    }
}
