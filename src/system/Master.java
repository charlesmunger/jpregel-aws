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
    private static final int NUM_PARTS_PER_PROCESSOR = 2;
    static private final Department[] departments = { ServiceImpl.ASAP_DEPARTMENT };
    static private Class[][] command2DepartmentArray = {
        // ASAP Commands
        {
            CommandComplete.class,
            GarbageCollected.class,
            InputFileProcessingComplete.class,
            JobSet.class,
            SuperStepComplete.class,
            WorkerMapSet.class,
            WorkerOutputWritten.class
        }
    };
        
    // Master attributes
    private Map<Integer, Service> integerToWorkerMap = new HashMap<Integer, Service>();
    protected AtomicInteger numRegisteredWorkers = new AtomicInteger();
    private volatile int numProcessorsPerWorker;
    // computation control
    protected int numUnfinishedWorkers;
    protected boolean commandExeutionIsComplete;
    protected boolean thereIsANextStep;
    
    // TODO: use this in init instead of what is there now. 
    // Must ensure that registrations are not lost before barrier is set to numWorkers
    private Barrier barrierWorkerRegistrationDone; 
    private Barrier barrierWorkerMapSet;
    private Barrier barrierWorkerJobSet;
    private Barrier barrierGraphMade;
    private Barrier barrierGarbageCollected;
    private Barrier barrierSuperStepDone;
    private Barrier barrierWorkerOutputWritten;
    
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
    public synchronized void init(int numWorkers) throws RemoteException, InterruptedException 
    {
        super.setService(this);
        super.setDepartments(departments);
        numUnfinishedWorkers += numWorkers;
        out.println("Master.makeWorkers: waiting for Worker registration to complete");
        if (numUnfinishedWorkers > 0 && !commandExeutionIsComplete) 
        {
            System.out.println("Master.makeWorkers: about to wait: numUnfinishedWorkers: " + numUnfinishedWorkers);
            wait(); // until numUnfinishedWorkers == 0
        }
        setWorkerMap();
    }
    
    @Override
    public synchronized void setWorkerMap() throws InterruptedException 
    {
        // broadcaast to workers: set your integerToWorkerMap
        barrierWorkerMapSet = new Barrier( integerToWorkerMap.size() );
        Command command = new SetWorkerMap(integerToWorkerMap);
        barrierComputation( command, barrierWorkerMapSet );
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
    public JobRunData run(Job job) throws InterruptedException
    {  
        // all Workers have registered with Master
        assert integerToWorkerMap.size() == numRegisteredWorkers.get();
        
        // initialize job statistics gathering
        job = new Job( job, numRegisteredWorkers.get() * numProcessorsPerWorker * NUM_PARTS_PER_PROCESSOR );
        JobRunData jobRunData = new JobRunData(job, integerToWorkerMap.size());
        initJob();
        String jobDirectoryName = job.getJobDirectoryName();
        FileSystem fileSystem = makeFileSystem( jobDirectoryName );
        
        // broadcaast to workers: set your Job & FileSystem
        barrierWorkerJobSet = new Barrier( integerToWorkerMap.size() );
        Command command = new SetJob(job);
        broadcast(command, this);

        // while workers SetJob, read Master input file, write Worker input files
        job.readJobInputFile(fileSystem, integerToWorkerMap.size() );
          
        // wait for all workers to SetJob before proceeding
        barrierWorkerJobSet.guard(); 
        jobRunData.setEndTimeSetWorkerJobAndMakeWorkerFiles();

        // broadcaast to workers: read your input file
        barrierGraphMade = new Barrier( integerToWorkerMap.size() );
        barrierComputation( new ReadWorkerInputFile(), barrierGraphMade );
        jobRunData.setEndTimeReadWorkerInputFile();
        
        // broadcaast to workers: Collect your garbage
        barrierGarbageCollected = new Barrier( integerToWorkerMap.size() );
        barrierComputation( new CollectGarbage(), barrierGarbageCollected );
        jobRunData.setEndTimeGarbageCollected();

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
            barrierSuperStepDone = new Barrier( integerToWorkerMap.size() );
            barrierComputation( startSuperStep, barrierSuperStepDone ); // broadcaast to workers: start a super step
            // BEGIN Post-step progress monitoring
            if (superStep % 10 == 0) 
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
        System.out.println("Master.run: writing worker output files.");
        barrierWorkerOutputWritten = new Barrier( integerToWorkerMap.size() );
        barrierComputation( new WriteWorkerOutputFile(), barrierWorkerOutputWritten );
        jobRunData.setEndTimeWriteWorkerOutputFiles();

        job.processWorkerOutputFiles(fileSystem, integerToWorkerMap.size());
        jobRunData.setEndTimeRun();
        
        return jobRunData;
    }
    
    /**
     * initialize master Job data structures
     */
    private void initJob() { numVertices = 0; }

    @Override
    public void shutdown(){} //Master deployment and shutdown is handled at the machine level. 


    /* _____________________________
     *  
     * Command implementations: Synchronize or explain why it is not needed!
     * _____________________________
     */
    // Command: CommandComplete
    public void commandComplete( int workerNum ) { processAcknowledgement(); }
    
    public void garbageCollected() { processAcknowledgement( barrierGarbageCollected ); }

    // Command: InputFileProcessingComplete
    synchronized public void inputFileProcessingComplete( int workerNum, int numVertices ) 
    {
        this.numVertices += numVertices;
        processAcknowledgement( barrierGraphMade );
    }
    
    public void workerOutputWritten() { processAcknowledgement( barrierWorkerOutputWritten ); }

    // Command: RegisterWorker
    synchronized public int registerWorker(ServiceName serviceName, int numWorkerProcessors ) 
    {
        assert serviceName != null;
        // !! currently not storing/using ServiceName data apart from Service

        // !! Ensure that no service with this ID is registered already.
        // !! If there is, unregister it.

        this.numProcessorsPerWorker = Math.max( this.numProcessorsPerWorker, numWorkerProcessors);
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
    public void superStepComplete(ComputeOutput computeOutput) 
    {
        thereIsANextStep |= computeOutput.getThereIsANextStep();
        numVertices += computeOutput.deltaNumVertices();
        stepAggregator.aggregate(computeOutput.getStepAggregator());
        problemAggregator.aggregate(computeOutput.getProblemAggregator());
        processAcknowledgement();
        processAcknowledgement( barrierSuperStepDone );
    }

    // Command: JobSet
    public void jobSet(int workerNum) { barrierWorkerJobSet.acknowledge(); }

    // Command: WorkerMapSet
    public void workerMapSet() { processAcknowledgement( barrierWorkerMapSet ); }
    
    void barrierComputation(Command command, Barrier barrier ) throws InterruptedException
    {
        broadcast( command, this );
        barrier.guard();
    }
    
    private void processAcknowledgement( Barrier barrier ) { barrier.acknowledge(); }
    
    synchronized private void processAcknowledgement() {
        if (--numUnfinishedWorkers == 0) {
            commandExeutionIsComplete = true;
            notify();
        }
    }

    public abstract FileSystem makeFileSystem(String jobDirectoryName);
    
    private class Barrier
    {
        private int n;
        
        Barrier( int n ) { this.n = n; }
        
        synchronized void acknowledge()
        {
            if ( --n == 0 )
            {
                notify();
            }
        }
        
        synchronized void guard() throws InterruptedException
        {
            if ( n > 0 )
            {
                wait();
            }
        }
    }
}
