package JpLAN;

import api.MachineGroup;
import java.io.IOException;
import system.Worker;

/**
 *
 * @author charlesmunger
 */
public class LANWorkerMachineGroup extends MachineGroup<Worker>
{

    private final int numWorkers;

    LANWorkerMachineGroup(int numberOfWorkers)
    {
        this.numWorkers = numberOfWorkers;
    }

    @Override
    public String getHostname()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void reset() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void terminate() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Worker syncDeploy(final String... args)
    {
        for (int i = 0; i < numWorkers; i++)
        {
            new Thread(new Runnable()
            {

                @Override
                public void run()
                {
                    try
                    {
                        Runtime.getRuntime().exec("java -server -cp ./dist/jpregel-aws.jar:./dist/lib/*  -Djava.security.policy=policy system.Worker " + args[0]);

                    } catch (IOException ex)
                    {
                        System.out.println("Failed to start java process." + ex.getLocalizedMessage());
                    }
                }
            }).start();
        }
        return null;
    }
}