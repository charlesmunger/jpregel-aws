/*
 * -  Is it worthwhile to send the Master the Worker numbers, thereby allowing
 *    the Master to avoid sending start super step commands to workers that
 *    have no active parts? Probably not worth the effort.
 */
// TODO: Worker: Batch add vertex requests?
//       Currently, when vertices are added during graph construction, a message
//       is sent for each vertex added (as opposed to batching all such requests
//       destined for the same worker). Is this best?
package system;

import static java.lang.System.err;
import static java.lang.System.exit;
import static java.lang.System.out;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import jicosfoundation.Command;
import jicosfoundation.CommandSynchronous;
import jicosfoundation.DefaultRemoteExceptionHandler;
import jicosfoundation.Department;
import jicosfoundation.Proxy;
import jicosfoundation.RemoteExceptionHandler;
import jicosfoundation.Service;
import jicosfoundation.ServiceImpl;
import system.commands.AddVertexToWorker;
import system.commands.AddVertexToPartComplete;
import system.commands.CommandComplete;
import system.commands.InputFileProcessingComplete;
import system.commands.MessageReceived;
import system.commands.ReadWorkerInputFile;
import system.commands.WriteWorkerOutputFile;
import system.commands.RegisterWorker;
import system.commands.SendMessage;
import system.commands.SendVertexIdToMessageQMap;
import system.commands.SetJob;
import system.commands.SetWorkerMap;
import system.commands.ShutdownWorker;
import system.commands.StartSuperStep;
import system.commands.SuperStepComplete;
import system.commands.JobSet;
import system.commands.WorkerMapSet;

/**
 *
 * @author Peter Cappello
 */
public final class Worker extends ServiceImpl
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
    
    private final Proxy masterProxy;
    private final int myWorkerNum;
    private final ComputeThread[] computeThreads;
    
    private Map<Integer, Service> workerNumToWorkerMap;
    private Job job;
    private Map<Integer, Part> partIdToPartMap = new HashMap<Integer, Part>();
    private Set<Part> partSet = new LinkedHashSet();
    private FileSystem fileSystem;
    private Map<Integer, Map<Object, MessageQ>> workerNumToVertexIdToMessageQMapMap;
    private long superStep;
    private Aggregator stepAggregator;
    private Aggregator problemAggregator;
    private int        deltaNumVertices;
    
    // coordination variables
    private boolean haveMultipleComputeThreads = true;
    private boolean thereIsANextStep;
    private AtomicInteger numUnacknowledgedAddVertexCommands = new AtomicInteger();
    private AtomicInteger numUnacknowledgedSendVertexIdToMessageQMaps = new AtomicInteger();
    private AtomicInteger numWorkingComputeThreads = new AtomicInteger();
    
    private PartIterator partIterator;
    
    // constants
    private final Command AddVertexToPartCompleteCommand = new AddVertexToPartComplete();
    private final Command MessageReceived = new MessageReceived();
    
    Worker( Service master ) throws RemoteException
    {
        // set Jicos Service attributes
        super( command2DepartmentArray );
        super.setService( this );
        super.setDepartments( departments );
        
        masterProxy = new ProxyMaster( master, this, REMOTE_EXCEPTION_HANDLER );
        CommandSynchronous command = new RegisterWorker( serviceName() ); 
        myWorkerNum = (Integer) master.executeCommand( this, command );
        super.register ( master );
        
        int numAvailableProcessors = Runtime.getRuntime().availableProcessors();
        if ( ! haveMultipleComputeThreads )
        {
            numAvailableProcessors = 1;
        }
           
        System.out.println("Worker.constructor: Available processors: " + numAvailableProcessors ) ; 
        computeThreads = new ComputeThread[ numAvailableProcessors ];
        for ( int i = 0; i < numAvailableProcessors; i++ )
        {
            computeThreads[i] = new ComputeThread( this, i );
        }
    }
     
    synchronized public void addVertexToPart( int partId, Vertex vertex )
    {
        Part part = partIdToPartMap.get( partId );
        if ( null == part )
        {
            part = new Part( partId, job );
            partSet.add( part );
            partIdToPartMap.put( partId, part );
        }
        part.add( vertex );
    }
    
    synchronized public FileSystem getFileSystem() { return fileSystem; }
    
    synchronized public Collection<Part> getParts() { return partIdToPartMap.values(); }
    
    synchronized public int getWorkerNum() { return myWorkerNum; }
        
    Set<Part> getPartSet() { return partSet; }
    
    synchronized PartIterator getPartIterator() { return partIterator; }
    
    synchronized public Job getJob() { return job; }
    
    int getWorkerNum( int partId )
    {
        int numWorkers = workerNumToWorkerMap.size();
        return ( partId % numWorkers ) + 1;
    }
      
//    synchronized public void addVertex( Vertex vertex, int partId, String stringVertex )
    synchronized public void addVertex( Vertex vertex, String stringVertex )
    {
        int partId = job.getPartId( vertex.getVertexId() );
        int workerNum = getWorkerNum( partId );
        if ( myWorkerNum == workerNum )
        {
            //out.println("Worker.addVertex: LOCAL: workerNum: " + workerNum + " partId: " + partId + " vertexId: " + vertex.getVertexId());
            addVertexToPart( partId, vertex );
        }
        else
        {
            Service workerService = workerNumToWorkerMap.get( workerNum );
//            out.println("Worker.addVertex: REMOTE: myWorkerNum: " + myWorkerNum + " workerNum: " + workerNum + " partId: " + partId + " vertexId: " + vertex.getVertexId() + " stringVertex: " + stringVertex);
            if ( workerService == null )
            {
                err.println("Worker.addVertex: NULL Worker: workerNum: " + workerNum );
                exit( 1 );
            }
            numUnacknowledgedAddVertexCommands.getAndIncrement();
            Command command = new AddVertexToWorker( partId, stringVertex, this );
            sendCommand( workerService, command );
        }
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
    
    private FileSystem makeFileSystem( boolean isEc2, String jobDirectoryName )
    {
        return ( isEc2 ) ? new Ec2FileSystem( jobDirectoryName, isEc2 ) : new LocalFileSystem( jobDirectoryName );
    }
    
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
    synchronized public void addVertexToWorker( int partId, String stringVertex, Service sendingWorker )
    {
        Vertex vertexFactory = job.getVertexFactory();
//        Combiner combiner    = job.getCombiner();
//        Vertex vertex = vertexFactory.make( stringVertex, combiner );
        Vertex vertex = vertexFactory.make( stringVertex );
        addVertexToPart( partId, vertex );
        sendCommand( sendingWorker, AddVertexToPartCompleteCommand );
    }
    
    // Command: AddVertexToPartComplete
    synchronized public void addVertexToPartComplete()
    {
        if ( numUnacknowledgedAddVertexCommands.decrementAndGet() == 0 )
        {
            notify();
        }
    }
    
    // Command: MessageReceived
    synchronized public void messageReceived()
    {
        if ( numUnacknowledgedSendVertexIdToMessageQMaps.decrementAndGet() == 0 )
        {
            notify();
        }
    }    
    
    // Command: ReadWorkerInputFile 
    synchronized public void processInputFile()
    { 
        int numVertices = job.makeGraph( this );
        
        // ensure that all AddVertexToPath Commands complete
        if ( numUnacknowledgedAddVertexCommands.get() > 0 )
        {
            try
            {
                wait();
            }
            catch( InterruptedException ignore ) {}
        }
        
        for ( ComputeThread computeThread : computeThreads )
        {
            computeThread.setPartIdToPartMap( partIdToPartMap );
        }
        Command command = new InputFileProcessingComplete( myWorkerNum, numVertices );
        masterProxy.execute( command );
        
        // output part sizes to see how PartId for vertices are distributed
        for ( Part part : partSet )
        {
            out.println("Worker: " + myWorkerNum  + " PartId: " + part.getPartId() + " size: " + part.getVertexIdToVertexMap().size() );
        }
    }
    
    // Command: SendMessage
    synchronized public void receiveMessage( Service sendingWorker, int partId, int vertexId, Message message, long superStep )
    {
        Part receivingPart = partIdToPartMap.get( partId );
        receivingPart.receiveMessage( vertexId, message, superStep );
        sendCommand( sendingWorker, MessageReceived );
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
    
    // Command: SetJob 
    synchronized public void setJob( Job job, boolean isEc2 )
    {
        superStep = -1L;
        this.job = job;
        String jobDirectoryName = job.getJobDirectoryName();
        fileSystem = makeFileSystem( isEc2, jobDirectoryName );
        job.setFileSystem( fileSystem );
        Command command = new JobSet( myWorkerNum );
        masterProxy.execute( command );
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
        masterProxy.execute( command );
     }
    
    // Command: ShutdownWorker
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
        masterProxy.execute( command );
    }
    
    // Command: WriteWorkerOutputFile
    public void writeWorkerOutputFile()
    {        
        job.makeOutputFile( this );        
        Command command = new CommandComplete( myWorkerNum );
        masterProxy.execute( command );
    }
    
    synchronized private void sync( AtomicInteger numUnacknowledgedSendVertexIdToMessageQMaps )
    {
        if ( numUnacknowledgedSendVertexIdToMessageQMaps.get() > 0 )
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
        sync( numUnacknowledgedSendVertexIdToMessageQMaps ); // wait for Vertex messaging to complete
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
    
    /*
     * Invoke to deploy a Worker on some machine
     * 
     * @param args [0]: Domain Name of machine on which Master is running
     */
    public static void main( String[] args ) throws RemoteException
    {
        System.setSecurityManager( new RMISecurityManager() );
        
        // get reference to Master
        if ( 1 != args.length )
        {
            out.println("java " + Worker.class.getName() + " MasterDomainName");
        }
        String masterDomainName = args[0];
        Service master = getMaster( masterDomainName );          
        new Worker( master ); // why is this not garbage immediately?
        out.println( "Worker: Ready." );
    }
    
    public static Service getMaster( String masterDomainName )
    {
        String url = "//" + masterDomainName + ":" + Master.PORT + "/" + Master.SERVICE_NAME;	
        Service master = null;
        try 
        {
            master = (Service) Naming.lookup( url );
        }
        catch ( NotBoundException exception ) 
        {
            out.println( "NotBoundException: " + url + " -- " + exception.getMessage() );
        } 
        catch ( MalformedURLException exception ) 
        {
            out.println( "MalformedURLException: " + url + " -- " + exception.getMessage() );
        } 
        catch ( RemoteException exception ) 
        {
            out.println( "RemoteException: " + url + " -- " + exception.getMessage() );
        }
        return master;
    }
        
    synchronized void sendMessage( int partId, int vertexId, Message message, long superStep )
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
            Command command = new SendMessage( this, partId, vertexId, message, superStep );
            sendCommand( workerService, command );
        }
    }
    
    public void output()
    {
        job.makeOutputFile( this );
        Command command = new CommandComplete( myWorkerNum );
        masterProxy.execute( command );   
    }
}
