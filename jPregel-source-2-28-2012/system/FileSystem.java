package system;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

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

    protected boolean isEc2 ; 
    
    FileSystem( String jobDirectoryName , boolean isEc2 ) { this.jobDirectoryName = jobDirectoryName; this.isEc2 = isEc2 ;  }
    
    abstract public FileInputStream  getFileInputStream()  throws FileNotFoundException;
    
    abstract public FileOutputStream getFileOutputStream() throws FileNotFoundException;
    
    // Use this to read a Worker input file
    abstract public FileInputStream  getWorkerInputFileInputStream(   int WorkerNum ) throws FileNotFoundException;
    
    // Use this to write a Worker input file
    abstract public FileOutputStream getWorkerInputFileOutputStream(  int WorkerNum ) throws FileNotFoundException;
    
    // Use this to read a Worker output file
    abstract public FileInputStream  getWorkerOutputFileInputStream(  int WorkerNum ) throws FileNotFoundException;
    
    // Use this to write a Worker output file
    abstract public FileOutputStream getWorkerOutputFileOutputStream( int WorkerNum ) throws FileNotFoundException;
    
    abstract public boolean getFileSystem()  ; 
    
    abstract public String getJobDirectory() ; 


}