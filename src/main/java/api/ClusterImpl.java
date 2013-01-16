package api;

import edu.ucsb.jpregel.system.ClientToMaster;
import edu.ucsb.jpregel.system.Job;
import edu.ucsb.jpregel.system.JobRunData;
import edu.ucsb.jpregel.system.Worker;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * A clique network comprising master (running an internal worker) and a distributed set of workers.
 * @author charlesmunger
 */
public class ClusterImpl extends UnicastRemoteObject implements Cluster
{
    private transient ClientToMaster master;
    private final MachineGroup<ClientToMaster> masterMachine;
    private final MachineGroup<Worker> workerMachine;
    private final String[] args;

    public ClusterImpl(ReservationService rs,String master, String worker, int numWorkers) throws InterruptedException, ExecutionException, IOException 
    {
        Future<MachineGroup<ClientToMaster>> masterMachineFut = rs.reserveMaster(master);
        Future<MachineGroup<Worker>> workers = rs.reserveWorkers(worker, numWorkers);
        this.args = new String[] {Integer.toString(numWorkers)};
        this.masterMachine = masterMachineFut.get();
        Future<ClientToMaster> deployMaster = masterMachine.deploy(args);
        this.workerMachine = workers.get();
        workerMachine.deploy(masterMachine.getHostname());
        this.master = deployMaster.get();
    }
      
    public ClusterImpl(MachineGroup<ClientToMaster> masterMachine, MachineGroup<Worker> workerMachine, String[] args) throws IOException, InterruptedException, ExecutionException
    {
        this.args = args;
        this.masterMachine = masterMachine;
        this.workerMachine = workerMachine;
        Future<ClientToMaster> deployMaster = masterMachine.deploy(args);
        workerMachine.deploy(masterMachine.getHostname());
        this.master = deployMaster.get();
    }
    
    public static Cluster getCluster() throws NotBoundException, MalformedURLException, RemoteException
    {
        String url = "rmi://localhost:" + Cluster.PORT+ "/" + Cluster.SERVICE_NAME;
        return (Cluster) Naming.lookup( url );
    }
    
    @Override
    public void register() throws RemoteException
    {
        Registry registry = LocateRegistry.createRegistry( Cluster.PORT );
        registry.rebind(SERVICE_NAME, this);
    }
    
    @Override
    synchronized public void reset()  throws IOException, InterruptedException, ExecutionException 
    {
        masterMachine.reset();
        workerMachine.reset();
        Future<ClientToMaster> deploy = masterMachine.deploy(args);
        workerMachine.deploy(masterMachine.getHostname());
        this.master = deploy.get();
    }
    
    /**
     * Run a job and return its execution performance data.
     * @param job the job to be run
     * @return Execution performance data
     * @throws RemoteException
     * @throws InterruptedException
     */
    @Override
    synchronized public JobRunData run(Job job) throws RemoteException, InterruptedException 
    {
        return master.run(job);
    }
    
    @Override
    public JobRunData run(Job job, String localJobDirectoryPathName) throws RemoteException, InterruptedException
    {
        System.out.println("ClusterImpl.run: S3 job directory name: " + job.getJobDirectoryName());
//        new AmazonS3Client(PregelAuthenticator.get()).putObject( job.getJobDirectoryName(), "input", new File( localJobDirectoryPathName ) );
        return run(job);
    }

    @Override
    public void terminate() throws IOException 
    {
        masterMachine.terminate();
        workerMachine.terminate();
    }   
}
