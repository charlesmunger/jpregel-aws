package system;

import api.WorkerOutputMaker;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 *
 * @author Pete Cappello
 */
public class WorkerOutputMakerStandard implements WorkerOutputMaker
{
    private static final long serialVersionUID = 1L;

    @Override
    public void write(FileSystem fileSystem, Worker worker) throws IOException
    {
        int workerNum = worker.getWorkerNum();
        BufferedWriter bufferedWriter = fileSystem.getWorkerOutputFileOutputStream(workerNum);

        for (Part part : worker.getParts())
        {
            for (VertexImpl vertex : part.getVertices())
            {
                String vertexOutput = vertex.output();
                if ( ! "".equals(vertexOutput) )
                {
                    bufferedWriter.write( vertexOutput );
                    bufferedWriter.newLine();
                } 
            }
        }

        bufferedWriter.close();
    }
}