package workerGraphMakers;

import static java.lang.System.err;
import static java.lang.System.exit;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import JpAws.WorkerGraphFileIO;
import system.Combiner;
import system.FileSystem;
import system.GraphMaker;
import system.Vertex;
import system.Worker;
import system.WorkerJob;

public class PageRankWorkerGraphMaker implements GraphMaker 
{

    @Override
    public int makeGraph(Worker worker) 
    {
        int numVertices = 0;
        try
        {
    //        int fis_read = 0 ; 
            int workerNum       = worker.getWorkerNum();
            WorkerJob workerJob = worker.getWorkerJob();
            FileSystem fileSystem = workerJob.getFileSystem();
            Combiner combiner     = workerJob.getCombiner();
            Vertex vertexFactory  = workerJob.getVertexFactory();
            BufferedReader bufferedReader = null ; 
            DataInputStream dataInputStream =null ; 
            FileInputStream fileInputStream = null ; 
            String jobDirectoryName = null ; 

            boolean isEc2 = fileSystem.getFileSystem() ; 
            if (isEc2) 
            {
                jobDirectoryName = fileSystem.getJobDirectory() ; 
                WorkerGraphFileIO workerGraph = new WorkerGraphFileIO(workerNum) ; 
                bufferedReader = workerGraph.FileInput(jobDirectoryName) ; 
                //bufferedReader = S3FileSystem.WorkerFileInput(jobDirectoryName, workerNum) ;       
                //System.out.println(" in StandWorkerGraph.makegraph (): working") ; 
            }
            else 
            {
                // read file
    //            fis_read = 1  ; 
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
//            if(fis_read == 1)
//            { 
//            dataInputStream.close();
//            fileInputStream.close(); 
//            } 
            bufferedReader.close();
            if( ! isEc2 )
            { 
            dataInputStream.close();
            fileInputStream.close(); 
            } 
        }
        catch ( Exception exception )
        {
            err.println( "SimpleWorkerGraphMaker.makeGraph: Error: " + exception.getMessage());
            exception.printStackTrace();
            exit( 1 );
        }
	    //System.out.println("StandardMasterOutputMasker.write() exiting") ; 

        return numVertices;
    }
		
		
	

}
