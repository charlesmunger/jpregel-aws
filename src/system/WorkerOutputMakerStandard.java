package system;

import JpAws.WorkerGraphFileIO;
import api.WorkerOutputMaker;
import java.io.*;

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
        // open Worker file for output
        boolean isEc2 = fileSystem.getFileSystem();
        String jobDirectoryName = fileSystem.getJobDirectory();
        int workerNum = worker.getWorkerNum();

        FileOutputStream fileOutputStream = fileSystem.getWorkerOutputFileOutputStream(workerNum);
        DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(dataOutputStream));

        for (Part part : worker.getParts())
        {
            for (VertexImpl vertex : part.getVertices())
            {
//                bufferedWriter.write(vertex.output());
//                bufferedWriter.newLine();
            }
        }

        // close Worker output file
        bufferedWriter.close();
        dataOutputStream.close();
        fileOutputStream.close();

        if (isEc2)
        {
            WorkerGraphFileIO workerGraph = new WorkerGraphFileIO(workerNum);
            workerGraph.UploadFilesOntoS3(jobDirectoryName);
            //S3FileSystem.WorkerUploadFiles(jobDirectoryName, "out", workerNum) ; 
        }
    }
}