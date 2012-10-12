/*
 * -  Is it worthwhile to send the Master the Worker numbers, thereby allowing
 *    the Master to avoid sending start super step commands to workers that
 *    have no active parts? Probably not worth the effort.
 */
// TODO: Worker: Batch the sending of AddVertexToWorker commands?
//       Currently, when vertices are added during graph construction, a message
//       is sent for each vertex added (as opposed to batching all such requests
//       destined for the same worker). Is this best?

// TODO FIX jicosfoundation Processor thread invokes start() in its constructor
package system;

import api.Aggregator;
import java.io.IOException;
import static java.lang.System.out;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import jicosfoundation.*;
import system.commands.*;

/**
 *
 * @author Peter Cappello
 */
public abstract class Worker extends ServiceImpl
{
    // ServiceImpl attributes
    static public String SERVICE_NAME = "Master";
    static private final Department[] departments = { ServiceImpl.ASAP_DEPARTMENT };
    static private final Class[][] command2DepartmentArray =
    {   
        // ASAP Commands
        {
            AddVertexToWorker.class,
            AddVertexToPartComplete.class,
            CollectGarbage.class,
            MessageReceived.class,
            ReadWorkerInputFile.class,
            WriteWorkerOutputFile.class,
            SendMessage.class,
            SendVertexIdToMessageQMap.class,
            SetJob.class,
            SetWorkerMap.class,
            ShutdownWorker.class,
            StartSuperStep.class
        } 
    };
    
    private final static RemoteExceptionHandler REMOTE_EXCEPTION_HANDLER = new DefaultRemoteExceptionHandler();

    private static void tryAgain(int i)
    {
        System.out.println("Master not up yet. Trying again in 5 seconds...");
        try
        {
            Thread.sleep(5000);
        } catch (InterruptedException ex1)
        {
            System.out.println("Waiting interrupted, trying again immediately");
        }
    }
    
    private final Proxy masterProxy;
    private int myWorkerNum = 0;
    private final ComputeThread[] computeThreads;
    
    private Map<Integer, Service> workerNumToWorkerMap;
    private Job job;
    private ConcurrentMap<Integer, Part> partIdToPartMap; 
    private Collection<Part> partSet; 
    private FileSystem fileSystem;
    private Map<Integer, Map<Object, MessageQ>> workerNumToVertexIdToMessageQMapMap;
    private long superStep;
    private Aggregator stepAggregator;
    private Aggregator problemAggregator;
    private int        deltaNumVertices;
    
    // coordination variables
    private boolean thereIsANextStep;
    private AtomicInteger numUnacknowledgedAddVertexCommands; 
    private AtomicInteger numUnacknowledgedSendVertexIdToMessageQMaps;
    private AtomicInteger numWorkingComputeThreads;
    
    private PartIterator partIterator;
    
    // constants
    private final Command AddVertexToPartCompleteCommand = new AddVertexToPartComplete();
    private final Command MessageReceived = new MessageReceived();
    private final Service master;
    
    public Worker( Service master ) throws RemoteException
    {
        // set Jicos Service attributes
        super( command2DepartmentArray );
        super.setDepartments( departments );
        super.register(master);
        this.master = master;
        masterProxy = new ProxyMaster( master, this, REMOTE_EXCEPTION_HANDLER );
        addProxy(master, masterProxy);
                
        int numAvailableProcessors = Runtime.getRuntime().availableProcessors();
        System.out.println("Worker.constructor: Available processors: " + numAvailableProcessors ) ; 
        computeThreads = new ComputeThread[ numAvailableProcessors ];
        for ( int i = 0; i < computeThreads.length; i++ )
        {
            computeThreads[i] = new ComputeThread( this );
        }
    }
    
    public void init() throws RemoteException 
    {
        super.setService( this );
        CommandSynchronous command = new RegisterWorker( serviceName(), Runtime.getRuntime().availableProcessors() ); 
        myWorkerNum =((Integer) master.executeCommand( this, command )); 
        super.register ( master );
        startComputeThreads();
    }
    
    void startComputeThreads()
    {
        for ( ComputeThread computeThread : computeThreads )
        {
            computeThread.start();
        }
    }
 
    public void addVertexToPart( int partId, VertexImpl vertex )
    {
        Part part = partIdToPartMap.get( partId );
        if ( null == part )
        {
            part = new Part( partId, job );
            partIdToPartMap.putIfAbsent( partId, part );
        }
        partIdToPartMap.get(partId).add( vertex );
    }
        
    synchronized public Collection<Part> getParts() { return partIdToPartMap.values(); }
    
    public int getWorkerNum() { return myWorkerNum; }
        
    Collection<Part> getPartSet() { return partSet; }
    
    synchronized PartIterator getPartIterator() { return partIterator; }
    
    synchronized public Job getJob() { return job; }
    
    public int getWorkerNum( int partId )
    {
        int numWorkers = workerNumToWorkerMap.size();
        return ( partId % numWorkers ) + 1;
    }
      
    // TODO omit this method by converting all worker graph makers
    synchronized public void addVertex( VertexImpl vertex, String stringVertex )
    {
        int partId = job.getPartId( vertex.getVertexId() );
        int workerNum = getWorkerNum( partId );
        if ( myWorkerNum == workerNum )
        {   // vertex is local to this worker
            addVertexToPart( partId, vertex );
        }
        else
        {   // vertex belongs to another worker
            Service workerService = workerNumToWorkerMap.get( workerNum );
            numUnacknowledgedAddVertexCommands.getAndIncrement();
            Command command = new AddVertexToWorker( partId, stringVertex, getWorkerNum() );
            sendCommand( workerService, command ); 
        }
    }
    
    public void addRemoteVertex( int workerNum, int partId, String stringVertex )
    {
        Service workerService = workerNumToWorkerMap.get( workerNum );
        numUnacknowledgedAddVertexCommands.getAndIncrement();
        Command command = new AddVertexToWorker( partId, stringVertex, getWorkerNum());
        sendCommand( workerService, command ); 
    }
           
    synchronized void mergeMap( Map<Integer, Map<Object, MessageQ>> workerNumToVertexIdToMessageQMapMap )
    {
        if ( this.workerNumToVertexIdToMessageQMapMap == null )
        {
            this.workerNumToVertexIdToMessageQMapMap = workerNumToVertexIdToMessageQMapMap;
            return;
        }
        for ( Integer workerNum : workerNumToVertexIdToMessageQMapMap.keySet() )
        {
            Map<Object, MessageQ> vertexIdToMessageQMap = workerNumToVertexIdToMessageQMapMap.get( workerNum );
            Map<Object, MessageQ> workerVertexIdToMessageQMap = this.workerNumToVertexIdToMessageQMapMap.get( workerNum );
            if ( workerVertexIdToMessageQMap == null )
            {
                this.workerNumToVertexIdToMessageQMapMap.put( workerNum, vertexIdToMessageQMap );
            }
            else
            {
                for ( Object vertexId : vertexIdToMessageQMap.keySet() )
                {
                    MessageQ  newMessageQ = vertexIdToMessageQMap.get( vertexId );
                    MessageQ workerMessageQ = workerVertexIdToMessageQMap.get( vertexId );
                    if ( workerMessageQ == null )
                    {
                        workerVertexIdToMessageQMap.put( vertexId, newMessageQ );
                    }
                    else
                    {
                        workerMessageQ.addAll( newMessageQ );
                    }
                }
            }
        }
    }
    
    public abstract FileSystem makeFileSystem( String jobDirectoryName);
    
    @Override
    public void exceptionHandler( Exception exception )
    { 
        exception.printStackTrace(); 
        System.exit( 1 );
    }
    
    /* _____________________________
     *  
     * Command implementations
     * _____________________________
     */
    
    // Command: AddVertexToWorker
    public void addVertexToWorker( int partId, String stringVertex, int sendingWorkerNum)
    {
        VertexImpl vertexFactory = job.getVertexFactory();
        VertexImpl vertex = vertexFactory.make( stringVertex );
        addVertexToPart( partId, vertex );
        sendCommand( workerNumToWorkerMap.get(sendingWorkerNum), AddVertexToPartCompleteCommand );
    }
    
    // Command: AddVertexToPartComplete
     public void addVertexToPartComplete()
    {
        if ( numUnacknowledgedAddVertexCommands.decrementAndGet() == 0 )
        {
            synchronized(this) { notify(); }
        }
    }
     
     public void collectGarbage()
    {
        if (this.collectingGarbage())
        {
            System.gc();
        }
        Command command = new GarbageCollected();
        sendCommand(master, command);
     }
    
    // Command: MessageReceived
    public void messageReceived()
    {
        if ( numUnacknowledgedSendVertexIdToMessageQMaps.decrementAndGet() == 0 )
        {
            synchronized(this) { notify(); }
        }
    }    
    
    // Command: ReadWorkerInputFile 
    synchronized public void processInputFile() throws InterruptedException
    {
        int numVertices = job.makeGraph( this );
        
        // ensure completion of all AddVertexToPath Commands before notifying master
        if ( numUnacknowledgedAddVertexCommands.get() > 0 )
        {
            wait();
        }
        
        for ( ComputeThread computeThread : computeThreads )
        {
            computeThread.setPartIdToPartMap( partIdToPartMap );
        }
        Command command = new InputFileProcessingComplete( myWorkerNum, numVertices );
        sendCommand( master, command );
        
        // output part sizes to see how PartId for vertices are distributed
        for ( Part part : partSet )
        {
            out.println("Worker.processInputFile: worker: " + myWorkerNum  + " PartId: " + part.getPartId() + " size: " + part.getVertexIdToVertexMap().size() );
        }
    }
    
    // Command: SendMessage
    public void receiveMessage( int sendingWorkerNum, int partId, int vertexId, Message message, long superStep )
    {
        Part receivingPart = partIdToPartMap.get( partId );
        receivingPart.receiveMessage( vertexId, message, superStep );
        sendCommand( workerNumToWorkerMap.get(sendingWorkerNum), MessageReceived );
    }
    
    // Command: SendVertexIdToMessageQMap
    synchronized public void receiveVertexIdToMessageQMap( Service sendingWorker, Map<Object, MessageQ> vertexIdToMessageQMap, Long superStep )
    {
        for ( Object vertexId : vertexIdToMessageQMap.keySet() )
        {
            int partId = job.getPartId( vertexId );
            Part receivingPart = partIdToPartMap.get( partId );
            MessageQ messageQ = vertexIdToMessageQMap.get( vertexId );
            receivingPart.receiveMessageQ( vertexId, messageQ, superStep );
        }
        sendCommand( sendingWorker, MessageReceived );
    }
    
    // Command: SetJob: initialize Job data structures
    synchronized public void setJob( Job job)
    {
        this.job = job;
        partIdToPartMap = new ConcurrentHashMap<Integer, Part>();
        // TODO: Worker: partSet: should be just parts that have > 0 active vertices
        partSet = partIdToPartMap.values();
        numUnacknowledgedAddVertexCommands = new AtomicInteger(); 
        numUnacknowledgedSendVertexIdToMessageQMaps = new AtomicInteger();
        numWorkingComputeThreads = new AtomicInteger();
        superStep = -1L;
        fileSystem = makeFileSystem( job.getJobDirectoryName() );
        job.setFileSystem( fileSystem );
        for ( ComputeThread computeThread : computeThreads )
        {
            computeThread.initJob();
        }
        
        Command command = new JobSet( myWorkerNum );
        sendCommand( master, command );
     }
    
    // Command: SetJob 
    synchronized public void setWorkerMap( Map<Integer, Service> integerToWorkerMap )
    {
        this.workerNumToWorkerMap = integerToWorkerMap;
        Collection<Service> workerServiceCollection = integerToWorkerMap.values();
        for ( Service workerService : workerServiceCollection )
        {
            super.register( workerService );
            Proxy workerProxy = new ProxyWorker( workerService, this, REMOTE_EXCEPTION_HANDLER );
            addProxy( workerService, workerProxy );
        }
        
        Command command = new WorkerMapSet();
        sendCommand( master, command );
     }
    
    // Command: ShutdownWorker
    @Override
    public void shutdown()
    {
        out.println("Worker.shutdown: shutting down.");
        System.exit( 0 );
    }
    
    // Command: StartSuperStep
    public void startSuperStep( ComputeInput computeInput )
    {
        barrierComputation( computeInput );
        ComputeOutput computeOutput = new ComputeOutput( thereIsANextStep, stepAggregator, problemAggregator, deltaNumVertices );
        Command command = new SuperStepComplete( computeOutput );
        sendCommand( master, command );
    }
    
    // Command: WriteWorkerOutputFile
    public void writeWorkerOutputFile()
    {        
        try
        {
            job.makeOutputFile( this );
        } catch (IOException ex)
        {
            Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
        }
//        Command command = new CommandComplete( myWorkerNum );
        Command command = new WorkerOutputWritten();
        sendCommand( master, command );
    }
    
    synchronized private void sync( AtomicInteger numUnacknowledgedSendVertexIdToMessageQMaps )
    {
        while ( numUnacknowledgedSendVertexIdToMessageQMaps.get() > 0 )
        {
            try
            {
                wait(); // notified when all acknowldegments have been received
            }
            catch ( InterruptedException ignore ) {}
        }
    }
    
    synchronized private void barrierComputation( ComputeInput computeInput )
    { 
        thereIsANextStep = false;
        superStep++;
        partIterator = new PartIterator( partSet.iterator() ); // initialize thread-safe Part iterator       
        numWorkingComputeThreads.set( computeThreads.length );
        problemAggregator = job.makeProblemAggregator(); // construct new problem aggregator for this step
        stepAggregator    = job.makeStepAggregator(); // construct new step aggregator
        deltaNumVertices = 0;
                
        for ( int i = 0; i < computeThreads.length; i++ )
        {
            computeThreads[ i ].workIsAvailable( superStep, computeInput ); // notify ComputeThread
        }
        while ( numWorkingComputeThreads.get() > 0 )
        {
            try
            {
                wait(); // until all ComputeThreads complete
            }
            catch ( InterruptedException ignore ) {}
        }
        // send each other worker its messages generated by this worker
        int numWorkersSentMessages = workerNumToVertexIdToMessageQMapMap.size();
        numUnacknowledgedSendVertexIdToMessageQMaps.getAndAdd( numWorkersSentMessages );
        for (Integer workerNum : workerNumToVertexIdToMessageQMapMap.keySet() )
        {
            Map<Object, MessageQ> vertexIdToMessageQMap = workerNumToVertexIdToMessageQMapMap.get( workerNum );
            Command command = new SendVertexIdToMessageQMap( this, vertexIdToMessageQMap, superStep + 1 );
            Service worker = workerNumToWorkerMap.get( workerNum );
            sendCommand( worker, command );
        }
        workerNumToVertexIdToMessageQMapMap = null;    
        sync( numUnacknowledgedSendVertexIdToMessageQMaps ); // wait for VertexImpl messaging to complete
    }
    
    synchronized void computeThreadComplete( Map<Integer, Map<Object, MessageQ>> workerNumToVertexIdToMessageQMapMapboolean,
            ComputeOutput computeOutput )
    {
        mergeMap( workerNumToVertexIdToMessageQMapMapboolean );
        thereIsANextStep |= computeOutput.getThereIsANextStep();
        stepAggregator.aggregate( computeOutput.getStepAggregator() );
        problemAggregator.aggregate( computeOutput.getProblemAggregator() );
        deltaNumVertices += computeOutput.deltaNumVertices();
        if ( numWorkingComputeThreads.decrementAndGet() == 0 )
        {
            notify();
        }
    }
    
    public static Service getMaster( String masterDomainName )
    {
        String url = "//" + masterDomainName + ":" + Master.PORT + "/" + Master.SERVICE_NAME;	
        Service master = null;
        for (int i = 0;; i += 5000)
        {
            try
            {
                master = (Service) Naming.lookup(url);
            } catch (Exception ex)
            {
                tryAgain(i);
                continue;
            }
            break;
        }
        return master;
    }
        
    void sendMessage( int partId, int vertexId, Message message, long superStep )
    {
        Part receivingPart = partIdToPartMap.get( partId );
        if ( receivingPart != null )
        {
            receivingPart.receiveMessage( vertexId, message, superStep );
        }
        else
        {
            int workerNum = getWorkerNum( partId );
            Service workerService = workerNumToWorkerMap.get( workerNum );
            assert workerService != null;
            numUnacknowledgedSendVertexIdToMessageQMaps.getAndIncrement();
            Command command = new SendMessage( getWorkerNum(), partId, vertexId, message, superStep );
            sendCommand( workerService, command );
        }
    }
    
    public void output()
    {
        try
        {
            job.makeOutputFile( this );
        } catch (IOException ex)
        {
            Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
        }
        Command command = new CommandComplete( myWorkerNum );
        sendCommand( master, command );
    }

    protected boolean collectingGarbage()
    {
        return true;
    }
}
