package edu.ucsb.jpregel.system;

import java.io.Serializable;
import static java.lang.System.currentTimeMillis;
import java.text.DateFormat;
import java.util.Date;

/**
 * Repository of job quantities &amp; execution times of each job phase.
 *
 * @author Pete Cappello
 */
public class JobRunData implements Serializable
{
    private static final int NUM_PHASES = 7;
    private static final String[] PHASE_NAMES = 
    {
        "Parallel: Worker: Set Job; Master: Write Worker input files",
        "Worker: Read input file",
        "Worker: Collect Garbage",
        "Worker: Compute Graph Problem",
        "Worker: Write output file",
        "Master: Process worker output files"
    };
    private final String jobName;
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
        phaseTimes[ 0 ] = currentTimeMillis();
    }
            
    void setNumSuperSteps( long numSuperSteps ) { this.numSuperSteps = numSuperSteps; }
    
    void logPhaseEndTime() { phaseTimes[ ++phase ] = currentTimeMillis(); }
    
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
        string.append( "\n\nElapsed times in milliseconds: \n\t" );
        for ( int i = 1; i < phaseTimes.length; i++ )
        {
            string.append( phaseTimes[ i ] - phaseTimes[ i - 1 ] ).append( "\t:\t" );
            string.append( PHASE_NAMES[ i - 1 ] ).append( "\n\t" );
        }
        string.append( phaseTimes[ 6 ] - phaseTimes[ 0 ] ).append( "\t:\tTOTAL Job time\n\t" );
        string.append( ( phaseTimes[ 4 ] - phaseTimes[ 3 ] ) / numSuperSteps ).append( "\t:\tAverage per super step \n   " );
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
        for ( int i = 1; i < phaseTimes.length; i++ )
        {
            string.append( phaseTimes[ i ] - phaseTimes[ i - 1 ] ).append( ',' );
        }
        string.append( phaseTimes[ 6 ] - phaseTimes[ 0 ] ).append( ',' );
        string.append( ( phaseTimes[ 3 ] - phaseTimes[ 2 ] ) / numSuperSteps );
        return new String( string );
    }
}
