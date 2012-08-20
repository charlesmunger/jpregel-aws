/*
 * !! Workers should never stop on Exceptions.
 * !! Do not System.exit(1) on file reading exception. Throw an exception.
 */
package system;

import api.WorkerGraphMaker;
import java.io.BufferedReader;
import static java.lang.System.err;
import static java.lang.System.exit;

/**
 *
 * @author Pete Cappello
 */
public class WorkerGraphMakerStandard implements WorkerGraphMaker
{

    private static final long serialVersionUID = 1L;

    @Override
    public int makeGraph(Worker worker)
    {
        int numVertices = 0;
        try
        {
            int workerNum = worker.getWorkerNum();
            Job job = worker.getJob();
            FileSystem fileSystem = job.getFileSystem();
            VertexImpl vertexFactory = job.getVertexFactory();
            BufferedReader bufferedReader = fileSystem.getWorkerInputFileInputStream(workerNum);

            /*
             * String strLine; System.out.println ("contents from the file");
             * while ((strLine = bufferedReader.readLine()) != null) {
             * System.out.println (strLine); }
             */

            for (String line; (line = bufferedReader.readLine()) != null; numVertices++)
            {
                VertexImpl vertex = vertexFactory.make(line);
                worker.addVertex(vertex, line);
            }
            bufferedReader.close();
        } catch (Exception exception)
        {
            err.println("StandardWorkerGraphMaker.makeGraph: Error: " + exception.getMessage());
            exception.printStackTrace();
            exit(1);
        }
        return numVertices;
    }
}