package workerOutputMakers;

import static java.lang.System.err;
import static java.lang.System.exit;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import system.FileSystem;
import system.Worker;

import system.Part;
import system.Vertex;
import system.WorkerWriter;
import JpAws.S3FileSys; 
import JpAws.WorkerGraphFileIO;
import java.io.*;




/**
 *
 * @author Pete Cappello
 */
public class StandardWorkerOutputMaker implements WorkerWriter
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void write( FileSystem fileSystem, Worker worker )
    {
        try
        {
            // open Worker file for output
		    //System.out.println("StandardWorkerOutputMaker.write() starting") ; 
            boolean isEc2 = fileSystem.getFileSystem() ; 
        	String jobDirectoryName = fileSystem.getJobDirectory() ; 
            int workerNum = worker.getWorkerNum(); 
            (new File(jobDirectoryName+"/out")).mkdirs();
            
            FileOutputStream fileOutputStream = fileSystem.getWorkerOutputFileOutputStream( workerNum );
            DataOutputStream dataOutputStream = new DataOutputStream( fileOutputStream );
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter( dataOutputStream )); 
            //System.out.println("worker.getParts() + worker.getWorkerNum " + worker.getParts().size() + "  " + worker.getWorkerNum() ) ; 
            
            for ( Part part : worker.getParts() )
            {
            	//System.out.println(" workers output i'm writing: StandardWorkerOutputMaker.java: Part:part "  ) ;  

                for ( Vertex vertex : part.getVertices() )
                {
                	//System.out.println(" workers output i'm writing: StandardWorkerOutputMaker.java"  ) ;  
                    bufferedWriter.write( vertex.output() );
                    bufferedWriter.newLine();
                }
            }
            
            // close Worker output file
            bufferedWriter.close(); 
            dataOutputStream.close();
            fileOutputStream.close();
            
            if(isEc2)
            {
            	WorkerGraphFileIO workerGraph = new WorkerGraphFileIO(workerNum) ; 
            	workerGraph.UploadFilesOntoS3(jobDirectoryName) ;            
            	//S3FileSystem.WorkerUploadFiles(jobDirectoryName, "out", workerNum) ; 
            }
            

        }
        catch ( Exception exception )
        {
            err.println( "StandardWorkerOutput.write: Error: " + exception.getMessage());
            exit( 1 );
        }
	    //System.out.println("StandardWorkerOutputMaker.write()  exiting") ; 

    }
}