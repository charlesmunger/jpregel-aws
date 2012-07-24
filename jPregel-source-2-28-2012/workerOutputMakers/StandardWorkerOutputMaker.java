package workerOutputMakers;

import JpAws.WorkerGraphFileIO;
import java.io.*;
import static java.lang.System.err;
import static java.lang.System.exit;
import system.FileSystem;
import system.*;

/**
 *
 * @author Pete Cappello
 */
public class StandardWorkerOutputMaker implements WorkerWriter
{
    private static final long serialVersionUID = 1L;

    public void write( FileSystem fileSystem, Worker worker )
    {
        try
        {
            // open Worker file for output
            boolean isEc2 = fileSystem.getFileSystem() ; 
            String jobDirectoryName = fileSystem.getJobDirectory() ; 
            int workerNum = worker.getWorkerNum(); 
            (new File(jobDirectoryName + "/out")).mkdirs();
            
            FileOutputStream fileOutputStream = fileSystem.getWorkerOutputFileOutputStream( workerNum );
            DataOutputStream dataOutputStream = new DataOutputStream( fileOutputStream );
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter( dataOutputStream )); 
            //System.out.println("worker.getParts() + worker.getWorkerNum " + worker.getParts().size() + "  " + worker.getWorkerNum() ) ; 
            
            for ( Part part : worker.getParts() )
            {
                for ( Vertex vertex : part.getVertices() )
                {
                    // TODO: Does not seem to create out directory and files for EuclideanShortestPathLocalClient
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
            exception.printStackTrace();
            exit( 1 );
        }
    }
}