package masterOutputMakers;

import static java.lang.System.err;
import static java.lang.System.exit;
import static java.lang.System.out;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import system.FileSystem;
import system.Writer;
import JpAws.S3FileSys; 
import JpAws.S3MasterOutputMaker;
import java.io.*;


/**
 *
 * @author Peter Cappello
 */
public class StandardMasterOutputMaker implements Writer
{
    @Override
    public void write(FileSystem fileSystem, int numWorkers)
    {
        try
        {    	
            (new File("output")).mkdir();
            // open Master file for output
        	int fis_read = 0 ;
            BufferedReader bufferedReader = null ; 
    	    DataInputStream dataInputStream =null ; 
    	    FileInputStream fileInputStream = null ; 
    	    boolean isEc2 = fileSystem.getFileSystem() ; 
		    String jobDirectoryName = fileSystem.getJobDirectory() ; 

		    //System.out.println("StandardMasterOutputMasker.write() starting") ; 
            FileOutputStream fileOutputStream = fileSystem.getFileOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream( fileOutputStream );
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter( dataOutputStream ) );
            //System.out.println("StandardMasterOpMaker.java: Got past BufferedWriter"); 
            for ( int fileNum = 1; fileNum <= numWorkers; fileNum++ )
            {
                if (isEc2) {
                	S3MasterOutputMaker masterOutputMaker = new S3MasterOutputMaker(fileNum) ; 
                	bufferedReader = masterOutputMaker.FileInput(jobDirectoryName) ; 
            	}
                else
                {            
                // open Worker output file for input
                fis_read = 1 ; 
                fileInputStream = fileSystem.getWorkerOutputFileInputStream( fileNum );
                dataInputStream = new DataInputStream( fileInputStream );
                bufferedReader = new BufferedReader( new InputStreamReader( dataInputStream )); } 
    
                for ( String line; ( line = bufferedReader.readLine() ) != null; )
                {
                    // append line to output file
                	//System.out.println("StandardMasterOpMaker" + line ) ; 
                    bufferedWriter.write( line );
                    bufferedWriter.newLine();
                }
                // close Worker output file
                if(fis_read == 1 ) 
                {
                	dataInputStream.close();
                    fileInputStream.close();
                }
                bufferedReader.close();
                
            }
            // close Master output file
            bufferedWriter.close();
            dataOutputStream.close();
            fileOutputStream.close();

            if(isEc2) 
            {
            	S3MasterOutputMaker masterOutputMaker = new S3MasterOutputMaker() ; 
            	masterOutputMaker.UploadFilesOntoS3(jobDirectoryName) ; 
            }
        }
        catch ( Exception exception )
        {
            err.println( "SimpleFileWriter.write: Error: " + exception.getMessage());
            exit( 1 );
        }
	    //System.out.println("StandardMasterOutputMasker.write() exiting") ; 

    } 
}