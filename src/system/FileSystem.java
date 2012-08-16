package system;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;

/**
 * FileSystem encapsulates the data & methods for interacting with a file
 * system, hiding whether it is a local file system or S3.
 * 
 * Job encapsulates the data structures & method implementations for a 
 * particular kind of graph problem, as well as parameter values for a 
 * particular instance of such a graph problem.
 * 
 * The number of Workers is an attribute, not of Job, but of the JPregel environment.
 * Whether or not a Job is run in development mode is an attribute, not of Job, but of the Client
 *
 * @author Peter Cappello
 */
abstract public class FileSystem
{
    protected String  jobDirectoryName;
    
    public FileSystem( String jobDirectoryName) { this.jobDirectoryName = jobDirectoryName;}
    
    abstract public BufferedReader  getFileInputStream()  throws FileNotFoundException;
    
    abstract public BufferedWriter getFileOutputStream() ;
    
    // Use this to read a Worker input file
    abstract public BufferedReader  getWorkerInputFileInputStream(   int WorkerNum ) throws FileNotFoundException;
    
    // Use this to write a Worker input file
    abstract public BufferedWriter getWorkerInputFileOutputStream(  int WorkerNum );
    
    // Use this to read a Worker output file
    abstract public BufferedReader  getWorkerOutputFileInputStream(  int WorkerNum ) throws FileNotFoundException;
    
    // Use this to write a Worker output file
    abstract public BufferedWriter getWorkerOutputFileOutputStream( int WorkerNum );
        
    public String getJobDirectory() {return jobDirectoryName;}
}