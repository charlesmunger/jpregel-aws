/*
 * !! Workers should never stop on Exceptions.
 * !! Do not System.exit(1) on file reading exception. Throw an exception.
 */
package workerGraphMakers;
import JpAws.WorkerGraphFileIO;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import static java.lang.System.err;
import static java.lang.System.exit;
import system.*;
/**
 *
 * @author Pete Cappello
 */
public class StandardWorkerGraphMaker implements GraphMaker
{
    private static final long serialVersionUID = 1L;

    public int makeGraph( Worker worker ) 
    {
        int numVertices = 0;
        try
        {
//            int fis_read = 0 ; 
            int workerNum       = worker.getWorkerNum();
            WorkerJob workerJob = worker.getWorkerJob();
            FileSystem fileSystem = workerJob.getFileSystem();
            Combiner combiner     = workerJob.getCombiner();
            Vertex vertexFactory  = workerJob.getVertexFactory();
            BufferedReader bufferedReader = null ; 
            DataInputStream dataInputStream =null ; 
            FileInputStream fileInputStream = null ; 
//            String jobDirectoryName = null ; 

            boolean isEc2 = fileSystem.getFileSystem() ; 
            if (isEc2) 
            {
                String jobDirectoryName = fileSystem.getJobDirectory() ; 
                WorkerGraphFileIO workerGraph = new WorkerGraphFileIO(workerNum) ; 
                bufferedReader = workerGraph.FileInput(jobDirectoryName) ; 
                //bufferedReader = S3FileSystem.WorkerFileInput(jobDirectoryName, workerNum) ;       
            }
            else 
            {
                // read file
//                fis_read = 1  ; 
                fileInputStream = fileSystem.getWorkerInputFileInputStream( workerNum );
                dataInputStream = new DataInputStream( fileInputStream );
                bufferedReader   = new BufferedReader(new InputStreamReader( dataInputStream ));  
            } 
        
            /*String strLine;
            System.out.println ("contents from the file"); 
            while ((strLine = bufferedReader.readLine()) != null)   {
                        System.out.println (strLine);
            } */
        	   
            for ( String line; ( line = bufferedReader.readLine() ) != null; numVertices++ )
            {
                Vertex vertex = vertexFactory.make( line, combiner );
//                worker.addVertex( vertex, workerJob.getPartId( vertex ), line );
                worker.addVertex( vertex, line );
            }
//            if (fis_read == 1)
//            { 
//                dataInputStream.close();
//                fileInputStream.close(); 
//            } 
            bufferedReader.close();
            if ( ! isEc2 )
            { 
                dataInputStream.close();
                fileInputStream.close(); 
            } 
        }
        catch ( Exception exception )
        {
            err.println( "StandardWorkerGraphMaker.makeGraph: Error: " + exception.getMessage());
            exception.printStackTrace();
            exit( 1 );
        }
        return numVertices;
    }
}