package system;

import api.Aggregator;
import api.MasterOutputMaker;
import api.MasterGraphMaker;
import api.WorkerGraphMaker;
import api.WorkerOutputMaker;
import java.io.IOException;
import java.io.Serializable;

/**
 * <p>
 * Job encapsulates the data structures & methods for a 
 * particular kind of graph problem, as well as parameter values for a 
 * particular instance of such a graph problem.
 * </p>
 * <p>
 * The number of Workers is an attribute, not of Job, but of the cluster on
 * which jpregel runs.
 * </p>
 * <p>
 * Whether or not a Job is run in local mode is an attribute, not of Job, but of the Client
 * </p>
 * 
 * @author Peter Cappello
 */
public final class Job implements Serializable
{
    private final String   jobName;
    private final String   jobDirectoryName;
    // TODO Job: Should this be set in client, as it is now?
    private final int      numParts;
    private       Aggregator stepAggregator    = new AggregatorNull();
    private       Aggregator problemAggregator = new AggregatorNull();
    private final VertexImpl   vertexFactory;
    private final WorkerOutputMaker workerOutputMaker;
    private final WorkerGraphMaker workerGraphMaker;
    private final MasterGraphMaker masterGraphMaker;
    private final MasterOutputMaker masterOutputMaker;
    
    private FileSystem fileSystem;

    /**
     * @param jobName The name used by jpregel when producing job run information.
     * @param jobDirectoryName The relative path to the job directory. 
     * If it is a local execution, the path is relative to the Netbeans project
     * (e.g., "examples/ShortestPath/test");
     * If it is an AWS execution, the path is relative to an S3 bucket.
     * The job directory is expected to have the <i>input</i> file; 
     * it will have the <i>output</i> file when the job is complete.
     * It also is used to contain intermediate directories <i>in</i> and <i>out</i>.
     * @param numParts the number of <i>parts</i> for this Job.
     * This is a Job attribute so that we can conveniently modify it
     * per Job, to find a good value that appears to be primarily a function of 
     * graph structure and size, and possibly problem type (e.g., shortest path)
     */
    public Job( String jobName, 
                String jobDirectoryName, 
                VertexImpl vertexFactory, 
                int numParts,
                MasterGraphMaker masterGraphMaker,
                WorkerGraphMaker workerGraphMaker,
                MasterOutputMaker masterOutputMaker,
                WorkerOutputMaker workerOutputMaker
            )
    {
        this.jobName               = jobName;
        this.jobDirectoryName      = jobDirectoryName;
        this.vertexFactory         = vertexFactory;
        this.numParts              = numParts;
        this.masterGraphMaker      = masterGraphMaker;
        this.workerGraphMaker      = workerGraphMaker;
        this.masterOutputMaker     = masterOutputMaker;
        this.workerOutputMaker     = workerOutputMaker;
    }
    
    public Job( String jobName, 
                String jobDirectoryName, 
                VertexImpl vertexFactory, 
                int numParts,
                MasterGraphMaker masterGraphMaker,
                WorkerGraphMaker workerGraphMaker,
                MasterOutputMaker masterOutputMaker,
                WorkerOutputMaker workerOutputMaker,
                Aggregator problemAggregator,
                Aggregator stepAggregator
            )
    {
        this.jobName           = jobName;
        this.jobDirectoryName  = jobDirectoryName;
        this.vertexFactory     = vertexFactory;
        this.numParts          = numParts;
        this.masterGraphMaker  = masterGraphMaker;
        this.workerGraphMaker  = workerGraphMaker;
        this.masterOutputMaker = masterOutputMaker;
        this.workerOutputMaker = workerOutputMaker;
        this.problemAggregator = problemAggregator;
        this.stepAggregator    = stepAggregator;
    }
    
    /*
     * @param masterJob - has Job attributes
     */
    Job( Job job )
    {
        jobName               = job.getJobName();
        jobDirectoryName      = job.getJobDirectoryName();
        vertexFactory         = job.getVertexFactory();
        numParts              = job.getNumParts();
        masterGraphMaker      = job.getMasterGraphMaker();
        workerGraphMaker      = job.getWorkerGraphMaker();
        masterOutputMaker     = job.getWriter();
        workerOutputMaker     = job.getWorkerWriter();
        stepAggregator        = job.getStepAggregator();
        problemAggregator     = job.getProblemAggregator(); 
    }  
        
    FileSystem getFileSystem() { return fileSystem; }
    
    int        getNumParts()   { return numParts; }
            
    int        getPartId( Object vertexId ) { return vertexFactory.getPartId( vertexId, getNumParts() ); }
    
    VertexImpl     getVertexFactory() { return vertexFactory; }
    
//    public void setProblemAggregator( Aggregator problemAggregator ) { this.problemAggregator = problemAggregator; }
//
//    public void setStepAggregator(    Aggregator stepAggregator )    { this.stepAggregator = stepAggregator; }
    
    /*
     * Used in JobRunData
     */
    String           getJobName()               { return jobName; }
    
    String           getJobDirectoryName()      { return jobDirectoryName; }
        
    Aggregator       getProblemAggregator()     { return problemAggregator; }
    
    Aggregator       getStepAggregator()        { return stepAggregator; }    
    
    WorkerGraphMaker getWorkerGraphMaker()      { return workerGraphMaker; }
    
    MasterGraphMaker getMasterGraphMaker()      { return masterGraphMaker; }
    
    WorkerOutputMaker getWorkerWriter()          { return workerOutputMaker; }
    
    MasterOutputMaker       getWriter()                { return masterOutputMaker; }
    
    /**
     * @return the number of vertices that were constructed.
     */
    int  makeGraph( Worker worker )      { return workerGraphMaker.makeGraph( worker ); }
    
    void makeOutputFile( Worker worker ) throws IOException { workerOutputMaker.write( fileSystem, worker ); }

    Aggregator  makeStepAggregator()            { return stepAggregator.make(); }
    
    Aggregator  makeProblemAggregator()         { return problemAggregator.make(); }
    
    /*
     * Process Result
     */
    void processMasterOutputFile() {} // !! implement: make file & produce jpeg visualization
    
    /*
     * Process Worker output files, numbered: 0 <= output file number < numWorkers
     * in directory <jobDirectoryName>/out/
     */
    void processWorkerOutputFiles( FileSystem fileSystem, int numWorkers )
    {
        masterOutputMaker.write( fileSystem, numWorkers );
    }
    
    /**
     * Write Worker input files <i>n</i>, for 0 &lt;= n &lt; numWorkers,
     * in directory <i>jobDirectoryName</i>/in/
     */
    void readJobInputFile( FileSystem fileSystem, int workerSetSize )
    {
        masterGraphMaker.make( fileSystem, workerSetSize );
    }
    
    void setFileSystem( FileSystem fileSystem ) { this.fileSystem = fileSystem; }
    
    @Override
    public String toString()
    {
        StringBuilder string = new StringBuilder();
        string.append("Job:\n\t");
        string.append("Name:                ").append(jobName).append("\n\t");
        string.append("Directory name:      ").append(jobDirectoryName).append("\n\t");
        string.append("Number of parts:     ").append(numParts).append("\n\t");
        string.append("Vertex factory:      ").append(vertexFactory.getClass().getCanonicalName()).append("\n\t");
        string.append("Master graph maker:  ").append(masterGraphMaker.getClass().getCanonicalName()).append("\n\t");
        string.append("Worker graph maker:  ").append(workerGraphMaker.getClass().getCanonicalName()).append("\n\t");
        string.append("Master output maker: ").append(masterOutputMaker.getClass().getCanonicalName()).append("\n\t");
        string.append("Worker output Maker: ").append(workerOutputMaker.getClass().getCanonicalName()).append("\n\t");
        string.append("Problem aggregator:  ").append(problemAggregator.getClass().getCanonicalName()).append("\n\t");
        string.append("Step aggregator:     ").append(stepAggregator.getClass().getCanonicalName()).append("\n\t");
        return new String( string );
    }
}
