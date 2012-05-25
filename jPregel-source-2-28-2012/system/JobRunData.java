package system;

import java.util.Date;

import static java.lang.System.currentTimeMillis;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 *
 * @author Pete Cappello
 */
public class JobRunData implements java.io.Serializable
{
    private static final java.text.DateFormat dateFormatter = new SimpleDateFormat("MMM d, yyyy @ HH:mm:ss" );
    private final String jobName;
    private final long beginRunTime;
    private final Date date;
    private final int numParts;
    private final boolean workerIsMultithreaded;
    private final Combiner combiner;
    private final int numWorkers;
    private final long maxMemory = Runtime.getRuntime().maxMemory();
    
    private String graphName;
    private long endTimeSetWorkerJobAndMakeWorkerFiles;
    private long endTimeReadWorkerInputFile;
    private long endTimeComputation;
    private long endTimeWriteWorkerOutputFiles;
    private long endTimeRun;
    private long numSuperSteps;
    
    public JobRunData( Job job, int numWorkers )
    {
        jobName   = job.getJobName();
        date      = new Date();
        numParts = job.getNumParts();
        workerIsMultithreaded = job.getWorkerIsMultithreaded();
        combiner = job.getCombiner();
        this.numWorkers = numWorkers;
        beginRunTime = currentTimeMillis();
    }
        
    void setEndTimeComputation() { endTimeComputation = currentTimeMillis(); }

    void setEndTimeReadWorkerInputFile() { endTimeReadWorkerInputFile = currentTimeMillis(); }
        
    void setEndTimeSetWorkerJobAndMakeWorkerFiles() { endTimeSetWorkerJobAndMakeWorkerFiles = currentTimeMillis(); }
    
    void setEndTimeWriteWorkerOutputFiles() { endTimeWriteWorkerOutputFiles = currentTimeMillis(); }

    void setEndTimeRun() { endTimeRun = currentTimeMillis(); }
    
    void setNumSuperSteps( long numSuperSteps ) { this.numSuperSteps = numSuperSteps; }
    
    public String toString()
    {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append( '\n' );
        stringBuffer.append( "\n_____________________________\n" );
        stringBuffer.append( '\n' );
        stringBuffer.append( jobName ).append( '\n' );
        stringBuffer.append( DateFormat.getDateTimeInstance( DateFormat.FULL, 
                                                             DateFormat.FULL ).format( date ) );
        stringBuffer.append( "\n   " ).append( numWorkers ).append( " Workers" );
        stringBuffer.append( "\n   " ).append( maxMemory / 1000  ).append( " Maximum memory (KB)" );
        stringBuffer.append( "\n   " ).append( numParts ).append( " Parts" );
        stringBuffer.append( "\n   Workers are ").append( workerIsMultithreaded ? "" : "NOT").append( " multithreaded" );
        stringBuffer.append( "\n   Messages will ").append( combiner != null ? "" : "NOT").append( " be combined\n   " );
        stringBuffer.append( numSuperSteps ).append( " super steps\n   " );

        stringBuffer.append( "\nElapsed times in milliseconds: \n   " );
        stringBuffer.append( endTimeRun - beginRunTime ).append( " : Overall run time\n   " );
        stringBuffer.append( endTimeSetWorkerJobAndMakeWorkerFiles - beginRunTime );
                stringBuffer.append( " : Set WorkerJob & make Worker files\n   " );
        stringBuffer.append( endTimeReadWorkerInputFile - endTimeSetWorkerJobAndMakeWorkerFiles );
        stringBuffer.append( " : Read Worker input files\n   " );
        stringBuffer.append( endTimeComputation - endTimeReadWorkerInputFile );
        stringBuffer.append( " : Computation\n   " );
        stringBuffer.append( (endTimeComputation - endTimeReadWorkerInputFile) / ( numSuperSteps + 1 ) );
        stringBuffer.append( " : average per super step \n   " );
        stringBuffer.append(  endTimeWriteWorkerOutputFiles - endTimeComputation );
        stringBuffer.append( " : Write Worker output files\n   " );
        
        stringBuffer.append( "\n_____________________________\n" );
        return new String( stringBuffer );
    }
    
    public String commaSeparatedValues()
    {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append( jobName ).append( ',' );
        return new String( stringBuffer );
    }
}
