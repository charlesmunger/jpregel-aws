package system;

import static java.lang.System.out;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


import jicosfoundation.Command;
import jicosfoundation.DefaultRemoteExceptionHandler;
import jicosfoundation.Department;
import jicosfoundation.Proxy;
import jicosfoundation.RemoteExceptionHandler;
import jicosfoundation.Service;
import jicosfoundation.ServiceImpl;
import jicosfoundation.ServiceName;
import system.commands.CommandComplete;

import JpAws.WorkerMachines;
import system.commands.InputFileProcessingComplete;
import system.commands.ReadWorkerInputFile;
import system.commands.WriteWorkerOutputFile;
import system.commands.SetWorkerJob;
import system.commands.SetWorkerMap;
import system.commands.StartSuperStep;
import system.commands.SuperStepComplete;
import system.commands.WorkerJobSet;
import system.commands.WorkerMapSet;

/**
 * Master is decoupled from MasterJob type: 
 * It is designed to handle all MasterJob subclasses.
 * 
 * Master.run is decoupled from its file system: It receives a FileSystem 
 * that hides the differences between a local file system & S3.
 * 
 * To quickly start workers, we start them from an EC2 machine.
 * If each worker was started from the client/administrator outside EC2, 
 * the total network latency (e.g., 1,000 Workers) would be unnecessarily high.
 * 
 * For now, Master IS responsible for Worker construction.
 * This eases speedup experiments with a specific number of Workers.
 * In a production (non-research) setting,
 * the Master would not be responsible for Worker construction. 
 * 
 * Ant tasks for Amazon Web Services jar files from
 *  https://github.com/crispywalrus/aws-tasks
 * 
 * Code mobility: jPregel comes with several job subclasses (e.g., shortest path).
 * The Master class path includes all these subclasses. By using a code base, 
 * a client can define a job that is not in the Master's classpath, by setting a
 * code base. These classes then can be downloaded by the Master via its RMI 
 * class loader.
 * 
 * @author Pete Cappello
 */
public class Master extends ServiceImpl implements ClientToMaster {

    // constants
    public static final RemoteExceptionHandler REMOTE_EXCEPTION_HANDLER = new DefaultRemoteExceptionHandler();
    // ServiceImpl attributes
    static public String SERVICE_NAME = "Master";
    static public int PORT = 2048;
    static private final Department[] departments = {ServiceImpl.ASAP_DEPARTMENT};
    static private Class[][] command2DepartmentArray = {
        // ASAP Commands
        {
            CommandComplete.class,
            InputFileProcessingComplete.class,
            WorkerJobSet.class,
            WorkerMapSet.class,
            SuperStepComplete.class
        }
    };
    // Master attributes
    private Map<Integer, Service> integerToWorkerMap = new HashMap<Integer, Service>();
    protected AtomicInteger numRegisteredWorkers = new AtomicInteger();
    // computation control
    protected int numUnfinishedWorkers;
    protected boolean commandExeutionIsComplete;
    protected boolean thereIsANextStep;
    // graph state
    protected Aggregator stepAggregator;
    protected Aggregator problemAggregator;
    protected int numVertices;

    public static void main(String[] args) throws RemoteException, AlreadyBoundException {
        System.setSecurityManager(new RMISecurityManager());
        Registry registry = LocateRegistry.createRegistry(Master.PORT);
        ClientToMaster master = new Master();
        registry.bind(SERVICE_NAME, master);
        out.println("Master: Ready.");
    }
    private WorkerMachines workerMachines;

    Master() throws RemoteException {
        // Establish Master as a Jicos Service
        super(command2DepartmentArray);
        super.setService(this);
        super.setDepartments(departments);
    }

    @Override
    public synchronized void makeWorkers(int numWorkers, String masterDomainName) throws RemoteException {
        System.out.println("Master.makeWorkers: entered: numWorkers: " + numWorkers);
        numUnfinishedWorkers += numWorkers;
        //Machine.startWorkers( masterDomainName, numWorkers, null );

        try {
            workerMachines = new WorkerMachines(masterDomainName);
            workerMachines.start(numWorkers);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        out.println("Master.makeWorkers: waiting for Worker registration to complete");

        // wait for all workers to Register before proceeding
        try {
            if (numUnfinishedWorkers > 0 && !commandExeutionIsComplete) {
                System.out.println("Master.makeWorkers: about to wait: numUnfinishedWorkers: " + numUnfinishedWorkers);
                wait(); // until numUnfinishedWorkers == 0
            }
        } catch (InterruptedException ignore) {
        }

        // broadcaast to workers: set your integerToWorkerMap
//        Command command = new SetWorkerMap( integerToWorkerMap );
//        barrierComputation( command );
        setWorkerMap();
    }

    public synchronized void setWorkerMap() {
        // broadcaast to workers: set your integerToWorkerMap
        Command command = new SetWorkerMap(integerToWorkerMap);
        barrierComputation(command);
    }

    @Override
    public void exceptionHandler(Exception exception) {
        exception.printStackTrace();
        System.exit(1);
    }

    /*
     * @param masterJob - what job type & instance to run
     * @param isEc2 - run mode: EC2 (true) | development (false)
     */
    public JobRunData run(Job job, boolean isEc2) {
        // all Workers have registered with Master
        assert integerToWorkerMap.size() == numRegisteredWorkers.get();

        JobRunData jobRunData = new JobRunData(job, integerToWorkerMap.size());

        // broadcaast to workers: set your Job & FileSystem
        WorkerJob workerJob = job.getWorkerJob();
        String jobDirectoryName = job.getJobDirectoryName();
        FileSystem fileSystem = makeFileSystem(isEc2, jobDirectoryName);
        numUnfinishedWorkers = integerToWorkerMap.size();
        commandExeutionIsComplete = false;
        Command command = new SetWorkerJob(workerJob, isEc2);
        broadcast(command, this);
        // while workers SetWorkerJob, read Job input file, write Worker input files
        job.readJobInputFile(fileSystem, integerToWorkerMap.size());

        // wait for all workers to SetWorkerJob before proceeding
        try {
            synchronized (this) {
                if (!commandExeutionIsComplete) {
                    wait(); // until numUnfinishedWorkers == 0 for SetWorkerJob
                }
            }
        } catch (InterruptedException ignore) {
        }
        jobRunData.setEndTimeSetWorkerJobAndMakeWorkerFiles();

        // broadcaast to workers: read your input file
        barrierComputation(new ReadWorkerInputFile());
        jobRunData.setEndTimeReadWorkerInputFile();

        // begin computation phase
        problemAggregator = workerJob.makeProblemAggregator();
        long superStep = 0;
        long startStepTime = System.currentTimeMillis(); // DEBUG
        long maxMemory = Runtime.getRuntime().maxMemory();
        for (thereIsANextStep = true; thereIsANextStep; superStep++) {
            // BEGIN Progress monitoring
            if (superStep % 1 == 0) {
                long endStepTime = System.currentTimeMillis();
                long elapsedTime = endStepTime - startStepTime;
                long freeMemory = Runtime.getRuntime().freeMemory();
                int percentFreeMemory = (int) (((float) freeMemory / maxMemory) * 100);
                out.println("SuperStep: " + superStep
                        + " requiring " + (elapsedTime / 1000) + " seconds "
                        + " Maximum memory that is free: " + percentFreeMemory + "%"
                        + " : " + (freeMemory / 1000) + "KB");
                startStepTime = endStepTime;
            }
            // END Progress monitoring
            thereIsANextStep = false;           // until a Worker says otherwise
            ComputeInput computeInput = new ComputeInput(stepAggregator, problemAggregator, numVertices);
            Command startSuperStep = new StartSuperStep(computeInput);
            stepAggregator = workerJob.makeStepAggregator(); // initialize stepAggregator
            barrierComputation(startSuperStep); // broadcaast to workers: start a super step
        }
        jobRunData.setEndTimeComputation();
        jobRunData.setNumSuperSteps(superStep);

        // broadcaast to workers: write your output file
        System.out.println("going to write workeroutputfile  ");
        barrierComputation(new WriteWorkerOutputFile());
        jobRunData.setEndTimeWriteWorkerOutputFiles();

        job.processWorkerOutputFiles(fileSystem, integerToWorkerMap.size());
        jobRunData.setEndTimeRun();

        return jobRunData;
    }

    @Override
    public void shutdown() {        
        // shutdown all Worker Services
        out.println("Master.shutdown: notifying Worker Services to shutdown.");
        //barrierComputation(new ShutdownWorker());
        try {
            workerMachines.Stop();
        } catch (IOException ex) {
            System.out.println("Exception shutting down workers. Check webUI for zombie instances.");
        }
        out.println("Master.shutdown: Worker Services shutdown.");

        // shutdown Master
        out.println("Master.shutdown: shutting down.");
    }

    /* _____________________________
     *  
     * Command implementations: Synchronize or know why it is not needed!
     * _____________________________
     */
    // Command: CommandComplete
    public void commandComplete(int workerNum) {
        processAcknowledgement();
    }

    // Command: InputFileProcessingComplete
    synchronized public void inputFileProcessingComplete(int workerNum, int numVertices) {
        this.numVertices += numVertices;
        processAcknowledgement();
    }

    // Command: RegisterWorker
    synchronized public int registerWorker(ServiceName serviceName) {
        assert serviceName != null;
        // !! currently not storing/using ServiceName data apart from Service

        // !! Ensure that no service with this ID is registered already.
        // !! If there is, unregister it.

        Service workerService = serviceName.service();
        super.register(workerService);
        Proxy workerProxy = new ProxyWorker(workerService, this, REMOTE_EXCEPTION_HANDLER);
        addProxy(workerService, workerProxy);
        int workerNum = numRegisteredWorkers.incrementAndGet();
        integerToWorkerMap.put(workerNum, workerService);
        processAcknowledgement();
        return workerNum;
    }

    // Command: SuperStepComplete
    public void superStepComplete(ComputeOutput computeOutput) {
        thereIsANextStep |= computeOutput.getThereIsANextStep();
        numVertices += computeOutput.deltaNumVertices();
        stepAggregator.aggregate(computeOutput.getStepAggregator());
        problemAggregator.aggregate(computeOutput.getProblemAggregator());
        processAcknowledgement();
    }

    // Command: WorkerJobSet
    public void workerJobSet(int workerNum) {
        processAcknowledgement();
    }

    // Command: WorkerMapSet
    public void workerMapSet() {
        processAcknowledgement();
    }

    synchronized protected void barrierComputation(Command command) {
        numUnfinishedWorkers = integerToWorkerMap.size();
        commandExeutionIsComplete = false;
        broadcast(command, this);
        try {
            if (!commandExeutionIsComplete) {
                wait(); // until all Workers complete
            }
        } catch (InterruptedException ignore) {
        }
    }

    synchronized private void processAcknowledgement() {
        if (--numUnfinishedWorkers == 0) {
            commandExeutionIsComplete = true;
            notify();
        }
    }

    private FileSystem makeFileSystem(boolean isEc2, String jobDirectoryName) {
        return (isEc2) ? new Ec2FileSystem(jobDirectoryName, isEc2) : new LocalFileSystem(jobDirectoryName);
    }
}
