package workerOutputMakers;

import JpAws.WorkerGraphFileIO;
import java.io.*;
import system.FileSystem;
import system.*;

/**
 *
 * @author Pete Cappello
 */
public class StandardWorkerOutputMaker implements WorkerWriter
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
            for (Vertex vertex : part.getVertices())
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