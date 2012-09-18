package api;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import system.ClientToMaster;
import system.Job;
import system.JobRunData;
import system.Worker;

/**
 *
 * @author charlesmunger
 */
public class Cluster implements Serializable
{

    private transient ClientToMaster master;
    private final MachineGroup<ClientToMaster> masterMachine;
    private final MachineGroup<Worker> workerMachine;
    private final String[] args;

    public Cluster(ReservationService rs,String master, String worker, int numWorkers) throws InterruptedException, ExecutionException, IOException {
        Future<MachineGroup<ClientToMaster>> masterMachineFut = rs.reserveMaster(master);
        Future<MachineGroup<Worker>> workers = rs.reserveWorkers(worker, numWorkers);
        this.args = new String[] {Integer.toString(numWorkers)};
        this.masterMachine = masterMachineFut.get();
        Future<ClientToMaster> deployMaster = masterMachine.deploy(args);
        this.workerMachine = workers.get();
        workerMachine.deploy(masterMachine.getHostname());
        this.master = deployMaster.get();
    }

    synchronized public JobRunData run(Job job) throws RemoteException, InterruptedException {
        return master.run(job);
    }

    public Cluster(MachineGroup<ClientToMaster> masterMachine, MachineGroup<Worker> workerMachine, String[] args) throws IOException, InterruptedException, ExecutionException
    {
        this.args = args;
        this.masterMachine = masterMachine;
        this.workerMachine = workerMachine;
        Future<ClientToMaster> deployMaster = masterMachine.deploy(args);
        workerMachine.deploy(masterMachine.getHostname());
        this.master = deployMaster.get();
    }
    
    public void terminate() throws IOException {
        masterMachine.terminate();
        workerMachine.terminate();
    }

    synchronized public void reset()  throws IOException, InterruptedException, ExecutionException {
        masterMachine.reset();
        workerMachine.reset();
        Future<ClientToMaster> deploy = masterMachine.deploy(args);
        workerMachine.deploy(masterMachine.getHostname());
        this.master = deploy.get();
    }
    
    private Object readResolve() throws ObjectStreamException {
        try
        {
            this.reset();
        } catch (IOException ex)
        {
            
        } catch (InterruptedException ex)
        {
            
        } catch (ExecutionException ex)
        {
            
        }
        return this;
    }
}
