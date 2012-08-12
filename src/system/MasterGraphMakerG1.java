package system;

import api.MasterGraphMaker;
import JpAws.S3MasterInputMaker;
import java.io.*;
import static java.lang.System.err;
import static java.lang.System.exit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pete Cappello
 */
public class MasterGraphMakerG1 implements MasterGraphMaker
{

    @Override
    public void make(FileSystem fileSystem, int numWorkers)
    {
        BufferedReader bufferedReader = null;
        DataInputStream dataInputStream = null;
        FileInputStream fileInputStream = null;
        String jobDirectoryName = null;

        boolean isEc2 = fileSystem.getFileSystem();
        if (isEc2)
        {
            jobDirectoryName = fileSystem.getJobDirectory();
            S3MasterInputMaker masterFileMaker = new S3MasterInputMaker();
            bufferedReader = masterFileMaker.FileInput(jobDirectoryName);
        } else // use local file system
        {
            try
            {
                fileInputStream = fileSystem.getFileInputStream();
            } catch (FileNotFoundException ex)
            {
                System.out.println("Error getting local filesystem input stream: " + ex.getLocalizedMessage());
            }
            dataInputStream = new DataInputStream(fileInputStream);
            bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));
        }
        String line = null;
        try
        {
            if ((line = bufferedReader.readLine()) == null)
            {
                err.println("WorkerFileWriter1: Error: input file has no lines.");
                exit(1);
            }
        } catch (IOException ex)
        {
            System.out.println("Error reading lines from file" + ex.getLocalizedMessage());
        }
        int numV = Integer.parseInt(line);
        if (!isEc2)
        {
            try
            {
                fileInputStream.close();
                dataInputStream.close();
            } catch (IOException ex)
            {
                System.out.println("Error closing input streams" + ex.getLocalizedMessage());
            }
        }
        int vertexNum = 1;
        for (int fileNum = 1; fileNum <= numWorkers; fileNum++)
        {
            // open file for output in "in" directory
            FileOutputStream fileOutputStream = null;
            try
            {
                fileOutputStream = fileSystem.getWorkerInputFileOutputStream(fileNum);
            } catch (FileNotFoundException ex)
            {
                System.out.println("Error getting output file stream: " + ex.getMessage());
                System.exit(1);
            }
            DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(dataOutputStream));

            int linesPerFile = numV / numWorkers;
            if (fileNum <= numV % numWorkers)
            {
                linesPerFile++;
            }

            for (int lineNum = 0; lineNum < linesPerFile; lineNum++)
            {
                // create line for vertex whose number is vertexNum
                StringBuilder string = new StringBuilder();
                string.append(vertexNum).append(' ');
                for (int targetVertexNum = vertexNum + 1; targetVertexNum <= numV; targetVertexNum++)
                {
                    string.append(targetVertexNum).append(' ');
                    int value = (targetVertexNum == vertexNum + 1) ? -1 : 1;
                    string.append(value).append(' ');
                }
                String lines = new String(string);
                try
                {
                    // append line to output file
                    bufferedWriter.write(lines);
                    bufferedWriter.newLine();
                } catch (IOException ex)
                {
                    System.out.println("Error writing lines to file: " + ex.getLocalizedMessage());
                }
                vertexNum++;
            }
            try
            {
                bufferedWriter.close();
                dataOutputStream.close();
                fileOutputStream.close();
            } catch (IOException ex)
            {
                Logger.getLogger(MasterGraphMakerG1.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (isEc2)
            {
                S3MasterInputMaker masterFileMaker = new S3MasterInputMaker(fileNum);
                masterFileMaker.UploadFilesOntoS3(jobDirectoryName);
            }
        }
        try
        {
            bufferedReader.close();
        } catch (IOException ex)
        {
            System.out.println("Error closing bufferedReader: " + ex.getMessage());
        }
    }
}