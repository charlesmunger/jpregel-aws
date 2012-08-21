package system;

import api.MasterGraphMaker;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
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
        String line = null;
        try
        {
            bufferedReader = fileSystem.getFileInputStream();
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
        int vertexNum = 1;
        for (int fileNum = 1; fileNum <= numWorkers; fileNum++)
        {
            BufferedWriter bufferedWriter = fileSystem.getWorkerInputFileOutputStream(fileNum);

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
            } catch (IOException ex)
            {
                Logger.getLogger(MasterGraphMakerG1.class.getName()).log(Level.SEVERE, null, ex);
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