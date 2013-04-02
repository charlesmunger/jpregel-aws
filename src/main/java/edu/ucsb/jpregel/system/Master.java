package edu.ucsb.jpregel.system;

import api.Aggregator;
import edu.ucsb.jpregel.system.commands.*;
import static java.lang.System.out;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import jicosfoundation.*;

/**
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
    static private Class[][] command2DepartmentArray = 
    {
        {   // ASAP Commands  
            InputFileProcessingComplete.class,
            SuperStepComplete.class,
            MasterCommandCompleted.class
        }
    };
        
    // Master attributes
    private Map<Integer, Service> integerToWorkerMap = new HashMap<Integer, Service>();
    protected AtomicInteger numRegisteredWorkers = new AtomicInteger();
    private volatile int numProcessorsPerWorker;
    
    // flow control attributes
    protected int numUnfinishedWorkers; // TODO use CountDownLatch
    protected boolean commandExeutionIsComplete; // TODO use CountDownLatch
    private CountDownLatch countDownLatch; // Worker synchronization barrier controller
    protected AtomicBoolean thereIsANextStep;
    
    // graph state
    protected Aggregator stepAggregator;
    protected Aggregator problemAggregator;
    protected AtomicInteger numVertices;
    
    long maxMemory = Runtime.getRuntime().maxMemory();

    // set Master as a Jicos Service
    public Master() throws RemoteException { super( command2DepartmentArray ); }

    @Override
    public synchronized void init(int numWorkers) throws RemoteException, InterruptedException 
    {
        super.setService(this);
        super.setDepartments(departments);
        
        // Ensure that registrations are not lost before barrier is set to numWorkers
        numUnfinishedWorkers += numWorkers;
        if (numUnfinishedWorkers > 0 && ! commandExeutionIsComplete) 
        {
            wait(); // until numUnfinishedWorkers == 0
        }
        setWorkerMap();
    }
    
    @Override
    public synchronized void setWorkerMap() throws InterruptedException 
    {
        // broadcaast to workers: set your integerToWorkerMap
        Command command = new SetWorkerMap( integerToWorkerMap );
        barrier( command );
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
    public JobRunData run( Job job ) throws InterruptedException
    {  
        // all Workers have registered with Master
        assert integerToWorkerMap.size() == numRegisteredWorkers.get();
        
        // initialize job statistics gathering
        job = new Job( job, numRegisteredWorkers.get() * numProcessorsPerWorker * NUM_PARTS_PER_PROCESSOR );
        JobRunData jobRunData = new JobRunData(job, integerToWorkerMap.size());
        initJob();
        String jobDirectoryName = job.getJobDirectoryName();
        FileSystem fileSystem = makeFileSystem( jobDirectoryName );
        
        // broadcaast to workers: set Job & FileSystem
        countDownLatch = new CountDownLatch( integerToWorkerMap.size() );
        Command command = new SetJob( job );
        broadcast(command, this);

        // while workers SetJob, read Master input file, write Worker input files
        job.readJobInputFile(fileSystem, integerToWorkerMap.size() );
          
        // wait for all workers to complete SetJob command
        countDownLatch.await();
        jobRunData.setEndTimeSetWorkerJobAndMakeWorkerFiles();

        // broadcaast to workers: read your input file
        command = new ReadWorkerInputFile();
        barrier( command );
        jobRunData.setEndTimeReadWorkerInputFile();
        
        // broadcaast to workers: Collect your garbage
        collectWorkerGarbage();
        jobRunData.setEndTimeGarbageCollected();

        // computation phase
        problemAggregator = job.makeProblemAggregator();
        long superStep = 0;
        long startStepTime = System.currentTimeMillis(); // monitor step time
        thereIsANextStep = new AtomicBoolean( true );
        for ( ; thereIsANextStep.get(); superStep++ ) 
        {
            thereIsANextStep = new AtomicBoolean(); // false, until a Worker says otherwise
            ComputeInput computeInput = new ComputeInput( problemAggregator, numVertices.get() );
            stepAggregator = job.makeStepAggregator(); // initialize stepAggregator
            command = new StartSuperStep( computeInput );
            barrier( command );
            if (superStep % 1 == 0) 
            {
                startStepTime = monitorStepProgress( startStepTime, superStep );
            }
        }
        jobRunData.setEndTimeComputation();
        jobRunData.setNumSuperSteps( superStep );

        // broadcaast to workers: write your output file
        System.out.println("Master.run: writing worker output files.");
        command = new WriteWorkerOutputFile();
        barrier( command );
        jobRunData.setEndTimeWriteWorkerOutputFiles();

        job.processWorkerOutputFiles(fileSystem, integerToWorkerMap.size());
        jobRunData.setEndTimeRun();
        
        return jobRunData;
    }
    
    /**
     * initialize master Job data structures
     */
    private void initJob()
    { 
        numVertices = new AtomicInteger(); 
    }
    
    private long monitorStepProgress( long startStepTime, long superStep )
    {
        long endStepTime = System.currentTimeMillis();
        long elapsedTime = endStepTime - startStepTime;
        long freeMemory = Runtime.getRuntime().freeMemory();
        int percentFreeMemory = (int) (((float) freeMemory / maxMemory) * 100);
        out.println("SuperStep: " + superStep
                + " requiring " + (elapsedTime / 1000) + " seconds "
                + " Maximum memory that is free: " + percentFreeMemory + "%"
                + " : " + (freeMemory / 1000) + "KB");
        return endStepTime;
    }

    @Override
    public void shutdown(){} //Master deployment and shutdown is handled at the machine level. 

    /* _____________________________
     *  
     * Command implementations: Synchronize or explain why it is not needed!
     * _____________________________
     */    
    // Command: InputFileProcessingComplete
    public void inputFileProcessingComplete( int workerNum, int numVertices ) 
    {
        this.numVertices.addAndGet( numVertices );
        commandCompleted();
    }
        
    public void commandCompleted() { countDownLatch.countDown(); }

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
        System.out.println("Master.registerWorker: workerNum: " + workerNum);
        return workerNum;
    }

    // Command: SuperStepComplete: Needs to be thread-safe!
    public void superStepComplete(ComputeOutput computeOutput) 
    {
        thereIsANextStep.weakCompareAndSet( false, computeOutput.getThereIsANextStep() );
        numVertices.addAndGet( computeOutput.deltaNumVertices() );
        stepAggregator.aggregate(computeOutput.getStepAggregator());
        problemAggregator.aggregate(computeOutput.getProblemAggregator());
        commandCompleted();
    }
    
    protected void collectWorkerGarbage() throws InterruptedException
    {
        Command command = new CollectGarbage();
        barrier( command );
    }
    
    private void barrier( Command command ) throws InterruptedException
    {
        countDownLatch = new CountDownLatch( integerToWorkerMap.size() );
        broadcast( command, this );
        countDownLatch.await();
    }
        
    synchronized private void processAcknowledgement() 
    {
        if (--numUnfinishedWorkers == 0) 
        {
            commandExeutionIsComplete = true;
            notify();
        }
    }

    public abstract FileSystem makeFileSystem(String jobDirectoryName);
}
