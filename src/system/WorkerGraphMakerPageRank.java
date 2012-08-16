package system;

import api.WorkerGraphMaker;
import java.io.BufferedReader;
import static java.lang.System.err;
import static java.lang.System.exit;

public class WorkerGraphMakerPageRank implements WorkerGraphMaker 
{

    @Override
    public int makeGraph(Worker worker) 
    {
        int numVertices = 0;
        try
        {
    //        int fis_read = 0 ; 
            int workerNum       = worker.getWorkerNum();
            Job job = worker.getJob();
            FileSystem fileSystem = job.getFileSystem();
            VertexImpl vertexFactory  = job.getVertexFactory();
            BufferedReader bufferedReader = fileSystem.getWorkerInputFileInputStream( workerNum );
        
        	/*String strLine;
        	System.out.println ("contents from the file"); 
        	while ((strLine = bufferedReader.readLine()) != null)   {
        		  System.out.println (strLine);
        	} */
                    
        
            for ( String line; ( line = bufferedReader.readLine() ) != null; numVertices++ )
            {
                VertexImpl vertex = vertexFactory.make( line );
//                worker.addVertex( vertex, job.getPartId( vertex ), line );
                worker.addVertex( vertex, line );
            }
            bufferedReader.close(); 
        }
        catch ( Exception exception )
        {
            err.println( "SimpleWorkerGraphMaker.makeGraph: Error: " + exception.getMessage());
            exception.printStackTrace();
            exit( 1 );
        }

        return numVertices;
    }
}
