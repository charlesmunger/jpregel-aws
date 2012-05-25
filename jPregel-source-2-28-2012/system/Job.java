package system;

import java.io.Serializable;

/**
 * Job encapsulates the data structures & method implementations for a 
 * particular kind of graph problem, as well as parameter values for a 
 * particular instance of such a graph problem.
 * 
 * The number of Workers is an attribute, not of Job, but of the JPregel environment
 * Whether or not a Job is run in local mode is an attribute, not of Job, but of the Client
 * 
 * @author Peter Cappello
 */
public class Job implements Serializable
{
    private final String   jobName;
    private final String   jobDirectoryName;
    private final int      numParts;
    private final boolean  workerIsMultithreaded;
    private final Combiner combiner;
    private       Aggregator stepAggregator    = new NullAggregator();
    private       Aggregator problemAggregator = new NullAggregator();
    private final Vertex   vertexFactory;
    private final WorkerWriter workerWriter;
    private final GraphMaker workerGraphMaker;
    private final MasterGraphMaker masterGraphMaker;
    private final Writer writer;

    /*
     * @param numParts - this is a Job attribute so that we can conveniently 
     * modify it per Job, to find a good value that appears to be primarily a 
     * function of graph problem type.
     */
    public Job( String jobName, String jobDirectoryName, Vertex vertexFactory, 
            int numParts, boolean workerIsMultithreaded, Combiner combiner, 
                WorkerWriter workerWriter, GraphMaker workerGraphMaker,
                MasterGraphMaker reader, Writer writer )
    {
        this.jobName               = jobName;
        this.jobDirectoryName      = jobDirectoryName;
        this.vertexFactory         = vertexFactory;
        this.numParts              = numParts;
        this.workerIsMultithreaded = workerIsMultithreaded;
        this.combiner              = combiner;
        this.workerWriter          = workerWriter;
        this.workerGraphMaker      = workerGraphMaker;
        this.masterGraphMaker      = reader;
        this.writer                = writer;
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
        workerIsMultithreaded = job.getWorkerIsMultithreaded();
        combiner              = job.getCombiner();
        stepAggregator        = job.getStepAggregator();
        problemAggregator     = job.getProblemAggregator();
        workerWriter          = job.getWorkerWriter();
        workerGraphMaker      = job.getWorkerGraphMaker();
        masterGraphMaker      = job.getMasterGraphMaker();
        writer                = job.getWriter();
    }  
    
    public Combiner getCombiner()      { return combiner; }
    
    public Vertex   getVertexFactory() { return vertexFactory; }
    
    public void setProblemAggregator( Aggregator problemAggregator ) { this.problemAggregator = problemAggregator; }

    public void setStepAggregator(    Aggregator stepAggregator )    { this.stepAggregator = stepAggregator; }
    
    /*
     * Used in JobRunData
     */
    String           getJobName()               { return jobName; }
    
    String           getJobDirectoryName()      { return jobDirectoryName; }
    
    int              getNumParts()              { return numParts; }
        
    boolean          getWorkerIsMultithreaded() { return workerIsMultithreaded; }

    Aggregator       getProblemAggregator()     { return problemAggregator; }
    
    Aggregator       getStepAggregator()        { return stepAggregator; }    
    
    GraphMaker       getWorkerGraphMaker()      { return workerGraphMaker; }
    
    MasterGraphMaker getMasterGraphMaker()      { return masterGraphMaker; }
    
    /*
     * Make a WorkerJob
     * 
     * @return the WorkerJob associated with this Job
     */
    WorkerJob    getWorkerJob()             { return new WorkerJob( this ); }
    
    WorkerWriter getWorkerWriter()          { return workerWriter; }
    
    Writer       getWriter()                { return writer; }

    Aggregator   makeStepAggregator()       { return stepAggregator.make(); }
    
    Aggregator   makeProblemAggregator()    { return problemAggregator.make(); }
    
    /*
     * Process Result
     */
    public void processMasterOutputFile() {} // !! implement: make file & produce jpeg visualization
    
    /*
     * Process Worker output files, numbered: 0 <= output file number < numWorkers
     * in directory <jobDirectoryName>/out/
     */
    void processWorkerOutputFiles( FileSystem fileSystem, int numWorkers )
    {
        writer.write( fileSystem, numWorkers );
    }
    
     /*
     * Create Worker input files, numbered: 0 <= input file number < numWorkers
     * put them in directory <jobDirectoryName>/in/
     */
    void readJobInputFile( FileSystem fileSystem, int workerSetSize )
    {
        masterGraphMaker.make( fileSystem, workerSetSize );
    }   
}
