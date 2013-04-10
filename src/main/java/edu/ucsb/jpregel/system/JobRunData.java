package edu.ucsb.jpregel.system;

import static java.lang.System.currentTimeMillis;
import java.text.DateFormat;
import java.util.Date;

/**
 * Repository of job quantities &amp; execution time per job phase.
 *
 * @author Pete Cappello
 */
public class JobRunData implements java.io.Serializable
{
    private static final int NUM_PHASES = 6;
    private final String jobName;
    private final long beginRunTime;
    private final Date date;
    private final int numParts;
    private final int numWorkers;
    private final long maxMemory = Runtime.getRuntime().maxMemory();
    private long numSuperSteps;
    private long[] phaseTimes = new long[ NUM_PHASES ];
    private int phase;
    
    public JobRunData( Job job, int numWorkers )
    {
        jobName   = job.getJobName();
        date      = new Date();
        numParts = job.getNumParts();
        this.numWorkers = numWorkers;
        beginRunTime = currentTimeMillis();
    }
            
    void setNumSuperSteps( long numSuperSteps ) { this.numSuperSteps = numSuperSteps; }
    
    void logPhaseEndTime() { phaseTimes[ phase++ ] = currentTimeMillis(); }
    
    @Override
    public String toString()
    {
        StringBuilder string = new StringBuilder();
        string.append( "\n\n________________________________________\n\n" );
        string.append( jobName ).append( '\n' );
        string.append( DateFormat.getDateTimeInstance( DateFormat.FULL, DateFormat.FULL ).format( date ) );
        string.append( "\n   " ).append( numWorkers ).append( " Workers" );
        string.append( "\n   " ).append( maxMemory / (1024 * 1024) ).append( " Maximum memory (MB)" );
        string.append( "\n   " ).append( numParts ).append( " Parts" );
        string.append( "\n   ").append( numSuperSteps ).append( " super steps   " );
        string.append( "\n\nElapsed times in milliseconds: \n   " );
        string.append( phaseTimes[ 5 ] - beginRunTime ).append( " : Total run time\n   " );
        string.append( phaseTimes[ 0 ] - beginRunTime ).append( " : Set WorkerJob & make Worker files\n   " );
        string.append( phaseTimes[ 1 ] - phaseTimes[ 0 ] ).append( " : Read Worker input files\n   " );
        string.append( phaseTimes[ 2 ] - phaseTimes[ 1 ] ).append( " : Worker Garbage Collection\n   ");
        string.append( phaseTimes[ 3 ] - phaseTimes[ 2 ] ).append( " : Computation\n   " );
        string.append( ( phaseTimes[ 3 ] - phaseTimes[ 2 ] ) / numSuperSteps ).append( " : average per super step \n   " );
        string.append( phaseTimes[ 4 ] - phaseTimes[ 3 ] ).append( " : Write Worker output files\n   " );
        string.append( "\n________________________________________\n" );
        return new String( string );
    }
    
    public String commaSeparatedValues()
    {
        StringBuilder string = new StringBuilder();
        string.append( jobName ).append( ',' );
        string.append( numWorkers ).append( ',' );
        string.append( maxMemory ).append( ',' );
        string.append( numParts ).append( ',' );
        string.append( numSuperSteps ).append( ',' );
        string.append( phaseTimes[ 5 ] - beginRunTime ).append( ',' );
        string.append( phaseTimes[ 0 ] - beginRunTime ).append( ',' );
        string.append( phaseTimes[ 1 ] - phaseTimes[ 0 ] ).append( ',' );
        string.append( phaseTimes[ 2 ] - phaseTimes[ 1 ] ).append( ',' );
        string.append( phaseTimes[ 3 ] - phaseTimes[ 2 ] ).append( ',' );
        string.append( ( phaseTimes[ 3 ] - phaseTimes[ 2 ] ) / numSuperSteps ).append( ',' );
        string.append( phaseTimes[ 4 ] - phaseTimes[ 3 ] );
        return new String( string );
    }
}
