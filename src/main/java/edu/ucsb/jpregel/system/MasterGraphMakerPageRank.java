package edu.ucsb.jpregel.system;

import api.MasterGraphMaker;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import static java.lang.System.err;
import static java.lang.System.exit;

public class MasterGraphMakerPageRank implements MasterGraphMaker
{

    @Override
    public void make(FileSystem fileSystem, int numWorkers)
    {
        try
        {
            BufferedReader bufferedReader = fileSystem.getFileInputStream();
            String line;
            while ((line = bufferedReader.readLine()) == null)
            {
                err.println("WorkerFileWriter1: Error: input file has no lines.");
                exit(1);
            }
            int numV = Integer.parseInt(line);

            int vertexNum = 0;
            int fileNum;
            for (fileNum = 1; fileNum <= numWorkers; fileNum++)
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
                    int endVertexNum = vertexNum + linesPerFile;
                    string.append(vertexNum).append(' ').append(endVertexNum).append(' ').append(numV);
                    String lines = new String(string);

                    // append line to output file
                    bufferedWriter.write(lines);
                    bufferedWriter.newLine();
                    vertexNum++;
                }

                bufferedWriter.close();
            } 
        } catch (Exception exception)
        {
            err.println("G1MasterGraphMaker.read: Error:is happening here damm " + exception.getMessage());
            exit(1);
        }
    }
}
