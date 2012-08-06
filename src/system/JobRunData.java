package system;

import static java.lang.System.currentTimeMillis;
import java.text.DateFormat;
import java.util.Date;

/**
 *
 * @author Pete Cappello
 */
public class JobRunData implements java.io.Serializable
{
    private final String jobName;
    private final long beginRunTime;
    private final Date date;
    private final int numParts;
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
        StringBuilder string = new StringBuilder();
        string.append( '\n' );
        string.append( "\n_____________________________\n" );
        string.append( '\n' );
        string.append( jobName ).append( '\n' );
        string.append( DateFormat.getDateTimeInstance( DateFormat.FULL, 
                                                             DateFormat.FULL ).format( date ) );
        string.append( "\n   " ).append( numWorkers ).append( " Workers" );
        string.append( "\n   " ).append( maxMemory / 1000  ).append( " Maximum memory (KB)" );
        string.append( "\n   " ).append( numParts ).append( " Parts" );
        string.append( "\n   ").append( numSuperSteps ).append( " super steps   " );
        string.append( "\n\nElapsed times in milliseconds: \n   " );
        string.append( endTimeRun - beginRunTime ).append( " : Overall run time\n   " );
        string.append( endTimeSetWorkerJobAndMakeWorkerFiles - beginRunTime );
        string.append( " : Set WorkerJob & make Worker files\n   " );
        string.append( endTimeReadWorkerInputFile - endTimeSetWorkerJobAndMakeWorkerFiles );
        string.append( " : Read Worker input files\n   " );
        string.append( endTimeComputation - endTimeReadWorkerInputFile );
        string.append( " : Computation\n   " );
        string.append( (endTimeComputation - endTimeReadWorkerInputFile) / ( numSuperSteps + 1 ) );
        string.append( " : average per super step \n   " );
        string.append(  endTimeWriteWorkerOutputFiles - endTimeComputation );
        string.append( " : Write Worker output files\n   " );
        
        string.append( "\n_____________________________\n" );
        return new String( string );
    }
    
    public String commaSeparatedValues()
    {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append( jobName ).append( ',' );
        return new String( stringBuffer );
    }
}
