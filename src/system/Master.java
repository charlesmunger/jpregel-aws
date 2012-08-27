package system;

import api.Aggregator;
import static java.lang.System.out;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import jicosfoundation.*;
import system.commands.*;

/**
 * Code mobility: jPregel comes with several vertex subclasses.
 * The Master/Worker jar includes all these subclasses. By using a code base, 
 * a client can define a vertex subclass that is not in the Master's class path.
 * These classes then are downloaded by the Master via its RMI class loader.
 * 
 * @author Pete Cappello
 */
abstract public class Master extends ServiceImpl implements ClientToMaster 
{
    // constants
    public static final RemoteExceptionHandler REMOTE_EXCEPTION_HANDLER = new DefaultRemoteExceptionHandler();
    
    // ServiceImpl attributes
    final static public String SERVICE_NAME = "Master";
    public static final String CLIENT_SERVICE_NAME = "ClientToMaster";
    final static public int PORT = 2048;
    private static final int PARTS_PER_PROCESSOR = 2;
    static private final Department[] departments = {ServiceImpl.ASAP_DEPARTMENT};
    static private Class[][] command2DepartmentArray = {
        // ASAP Commands
        {
            CommandComplete.class,
            InputFileProcessingComplete.class,
            JobSet.class,
            WorkerMapSet.class,
            SuperStepComplete.class
        }
    };
        
    // Master attributes
    private Map<Integer, Service> integerToWorkerMap = new HashMap<Integer, Service>();
    protected AtomicInteger numRegisteredWorkers = new AtomicInteger();
    private volatile int numWorkerProcessors;
    // computation control
    protected int numUnfinishedWorkers;
    protected boolean commandExeutionIsComplete;
    protected boolean thereIsANextStep;
    
    // graph state
    protected Aggregator stepAggregator;
    protected Aggregator problemAggregator;
    protected int numVertices;

    

    public Master() throws RemoteException 
    {
        // Establish Master as a Jicos Service
        super(command2DepartmentArray);
    }

    @Override
    public synchronized void init(int numWorkers) throws RemoteException 
    {
        super.setService(this);
        super.setDepartments(departments);
        numUnfinishedWorkers += numWorkers;
        out.println("Master.makeWorkers: waiting for Worker registration to complete");
        try 
        {
            if (numUnfinishedWorkers > 0 && !commandExeutionIsComplete) 
            {
                System.out.println("Master.makeWorkers: about to wait: numUnfinishedWorkers: " + numUnfinishedWorkers);
                wait(); // until numUnfinishedWorkers == 0
            }
        } 
        catch (InterruptedException ignore) {}
        setWorkerMap();
    }
    
    @Override
    public synchronized void setWorkerMap() 
    {
        // broadcaast to workers: set your integerToWorkerMap
        Command command = new SetWorkerMap(integerToWorkerMap);
        barrierComputation(command);
    }

    @Override
    public void exceptionHandler(Exception exception) 
    {
        exception.printStackTrace();
        System.exit(1);
    }

    /*
     * @param Job   - problem and instance parameters
     */
    @Override
    public JobRunData run(Job job) 
    {
        System.out.println("Run entered");
        // all Workers have registered with Master
        assert integerToWorkerMap.size() == numRegisteredWorkers.get();
        System.out.println("Job being split into "+numWorkerProcessors*PARTS_PER_PROCESSOR*numRegisteredWorkers.get() +"parts");
        job = new Job(job,numWorkerProcessors*PARTS_PER_PROCESSOR*numRegisteredWorkers.get());

        JobRunData jobRunData = new JobRunData(job, integerToWorkerMap.size());

        // broadcaast to workers: set your Job & FileSystem
        String jobDirectoryName = job.getJobDirectoryName();
        FileSystem fileSystem = makeFileSystem( jobDirectoryName);
        numUnfinishedWorkers = integerToWorkerMap.size();
        commandExeutionIsComplete = false;
        Command command = new SetJob(job);
        broadcast(command, this);

        // while workers SetJob, read Job input file, write Worker input files
        job.readJobInputFile(fileSystem, integerToWorkerMap.size());

        try 
        {   // wait for all workers to SetJob before proceeding
            synchronized (this) 
            {
                if (!commandExeutionIsComplete) 
                {
                    wait(); // until numUnfinishedWorkers == 0 for SetJob
                }
            }
        } 
        catch (InterruptedException ignore) {}
        jobRunData.setEndTimeSetWorkerJobAndMakeWorkerFiles();

        // broadcaast to workers: set job & read your input file
        barrierComputation(new ReadWorkerInputFile());
        jobRunData.setEndTimeReadWorkerInputFile();

        // begin computation phase
        problemAggregator = job.makeProblemAggregator();
        long superStep = 0;
        long startStepTime = System.currentTimeMillis(); // Progress monitoring
        long maxMemory = Runtime.getRuntime().maxMemory();
        for (thereIsANextStep = true; thereIsANextStep; superStep++) 
        {
            thereIsANextStep = false;           // until a Worker says otherwise
            ComputeInput computeInput = new ComputeInput(stepAggregator, problemAggregator, numVertices);
            Command startSuperStep = new StartSuperStep(computeInput);
            stepAggregator = job.makeStepAggregator(); // initialize stepAggregator
            barrierComputation(startSuperStep); // broadcaast to workers: start a super step
            // BEGIN Progress monitoring
            if (superStep % 2 == 0) 
            {
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
    public void shutdown(){} //Master deployment and shutdown is handled at the machine level. 


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
        ProxyWorker workerProxy = new ProxyWorker(workerService, this, REMOTE_EXCEPTION_HANDLER);
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

    protected void setProcessorsPerWorker(final int numProcessors) {
        this.numWorkerProcessors = numProcessors;
    }
    
    // Command: JobSet
    public void jobSet(int workerNum) { processAcknowledgement(); }

    // Command: WorkerMapSet
    public void workerMapSet() { processAcknowledgement(); }

    synchronized protected void barrierComputation(Command command)
    {
        numUnfinishedWorkers = integerToWorkerMap.size();
        commandExeutionIsComplete = false;
        broadcast(command, this);
        try {
            if (!commandExeutionIsComplete) {
                wait(); // until all Workers complete
            }
        } catch (InterruptedException ignore) {}
    }

    synchronized private void processAcknowledgement() {
        if (--numUnfinishedWorkers == 0) {
            commandExeutionIsComplete = true;
            notify();
        }
    }

    public abstract FileSystem makeFileSystem(String jobDirectoryName);
}
