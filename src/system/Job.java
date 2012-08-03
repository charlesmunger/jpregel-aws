package system;

import java.io.Serializable;

/**
 * Job encapsulates the data structures & method implementations for a 
 * particular kind of graph problem, as well as parameter values for a 
 * particular instance of such a graph problem.
 * 
 * The number of Workers is an attribute, not of Job, but of the jpregel environment
 * Whether or not a Job is run in local mode is an attribute, not of Job, but of the Client
 * 
 * @author Peter Cappello
 */
public final class Job implements Serializable
{
    private final String   jobName;
    private final String   jobDirectoryName;
    private final int      numParts;
//    private final boolean  workerIsMultithreaded;
    private       Aggregator stepAggregator    = new NullAggregator();
    private       Aggregator problemAggregator = new NullAggregator();
    private final Vertex   vertexFactory;
    private final WorkerWriter workerWriter;
    private final GraphMaker workerGraphMaker;
    private final MasterGraphMaker masterGraphMaker;
    private final Writer writer;
    
    private FileSystem fileSystem;

    /*
     * @param numParts - a Job attribute so that we can conveniently modify it
     * per Job, to find a good value that appears to be primarily a function of 
     * graph structure and size, and possibly problem type (e.g., shortest path)
     */
    public Job( String jobName, 
                String jobDirectoryName, 
                Vertex vertexFactory, 
                int numParts, 
                WorkerWriter workerWriter, 
                GraphMaker workerGraphMaker,
                MasterGraphMaker reader, 
                Writer writer 
            )
    {
        this.jobName               = jobName;
        this.jobDirectoryName      = jobDirectoryName;
        this.vertexFactory         = vertexFactory;
        this.numParts              = numParts;
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
        stepAggregator        = job.getStepAggregator();
        problemAggregator     = job.getProblemAggregator();
        workerWriter          = job.getWorkerWriter();
        workerGraphMaker      = job.getWorkerGraphMaker();
        masterGraphMaker      = job.getMasterGraphMaker();
        writer                = job.getWriter();
    }  
        
    public FileSystem getFileSystem() { return fileSystem; }
    
    public int        getNumParts()   { return numParts; }
            
    public int        getPartId( Object vertexId ) { return vertexFactory.getPartId( vertexId, getNumParts() ); }
    
    public Vertex     getVertexFactory() { return vertexFactory; }
    
    public void setProblemAggregator( Aggregator problemAggregator ) { this.problemAggregator = problemAggregator; }

    public void setStepAggregator(    Aggregator stepAggregator )    { this.stepAggregator = stepAggregator; }
    
    /*
     * Used in JobRunData
     */
    String           getJobName()               { return jobName; }
    
    String           getJobDirectoryName()      { return jobDirectoryName; }
        
    Aggregator       getProblemAggregator()     { return problemAggregator; }
    
    Aggregator       getStepAggregator()        { return stepAggregator; }    
    
    GraphMaker       getWorkerGraphMaker()      { return workerGraphMaker; }
    
    MasterGraphMaker getMasterGraphMaker()      { return masterGraphMaker; }
    
    WorkerWriter getWorkerWriter()          { return workerWriter; }
    
    Writer       getWriter()                { return writer; }
    
    /*
     * @return the number of vertices that were constructed.
     */
    public int  makeGraph( Worker worker )      { return workerGraphMaker.makeGraph( worker ); }
    
    public void makeOutputFile( Worker worker ) { workerWriter.write( fileSystem, worker ); }

    Aggregator  makeStepAggregator()            { return stepAggregator.make(); }
    
    Aggregator  makeProblemAggregator()         { return problemAggregator.make(); }
    
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
    
    void setFileSystem( FileSystem fileSystem ) { this.fileSystem = fileSystem; }
    
    public String toString()
    {
        StringBuilder string = new StringBuilder();
        string.append("Job:\n\t");
        string.append("Name: ").append(jobName).append("\n\t");
        string.append("Directory name: ").append(jobDirectoryName).append("\n\t");
        string.append("Number of parts: ").append(numParts).append("\n\t");
        string.append("Step Aggregator: ").append(stepAggregator.getClass().getCanonicalName()).append("\n\t");
        string.append("Problem Aggregator: ").append(problemAggregator.getClass().getCanonicalName()).append("\n\t");
        string.append("Vertex factory: ").append(vertexFactory.getClass().getCanonicalName()).append("\n\t");
        string.append("Worker writer: ").append(workerWriter.getClass().getCanonicalName()).append("\n\t");
        string.append("Worker graph maker: ").append(workerGraphMaker.getClass().getCanonicalName()).append("\n\t");
        string.append("Master graph maker: ").append(masterGraphMaker.getClass().getCanonicalName()).append("\n\t");
        string.append("Writer: ").append(writer.getClass().getCanonicalName()).append("\n\t");
        return new String( string );
    }
}
