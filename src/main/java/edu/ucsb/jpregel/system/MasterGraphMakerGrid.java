/*
 * Makes an N x N Euclidean grid graph.
 * The sortest path from (0, 0) to (N - 1, N - 1) thus is 2(N - 1).
 * 
 * It produces files that have 1 line of the form:
 * N blockSize row col 
 * where 0 <= row, col < N and blockSize = N / sqrt( numWorkers )
 * 
 * A worker that receives that file should create the vertices and out edges for
 * the subgrid whose lower left vertex has coordinates is (r, c) and whose
 * upper right vertex has coordinates ( r + blockSize - 1, c + blockSize - 1 ).
 */
package edu.ucsb.jpregel.system;

import api.MasterGraphMaker;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import static java.lang.System.*;

/**
 *
 * @author Pete Cappello
 */
public class MasterGraphMakerGrid implements MasterGraphMaker
{

    @Override
    public void make(FileSystem fileSystem, int numWorkers)
    {
        try
        {
            BufferedReader bufferedReader = fileSystem.getFileInputStream();
            String line;
            if ((line = bufferedReader.readLine()) == null)
            {
                err.println("GridMasterGraphMaker: Error: input file has no lines.");
                exit(1);
            }

            int N = Integer.parseInt(line);
            int sqrtNumWorkers = (int) Math.sqrt(numWorkers);
            int blockSize = N / sqrtNumWorkers;
            System.out.println("GridMasterGraphMaker.make: N: " + N + " sqrtNumWorkers: " + sqrtNumWorkers + " blockSize: " + blockSize);

            for (int row = 0; row < sqrtNumWorkers; row++)
            {
                for (int col = 0; col < sqrtNumWorkers; col++)
                {
                    int fileNum = row * sqrtNumWorkers + col + 1; // fileNum = 0, 1, ... , numWorkers - 1

                    BufferedWriter bufferedWriter = fileSystem.getWorkerInputFileOutputStream(fileNum);

                    // line = "N blockSize row col" as described in preamble
                    StringBuilder string = new StringBuilder();
                    string.append(N).append(' ');
                    string.append(blockSize).append(' ');
                    string.append(row * blockSize).append(' ');
                    string.append(col * blockSize);

                    line = new String(string);
                    out.println("GridMasterGraphMaker: line: " + line);
                    bufferedWriter.write(line);

                    // close output file
                    bufferedWriter.close();
                }
            }
            bufferedReader.close();
            out.println("GridMasterGraphMaker: complete.");
        } catch (Exception exception)
        {
            err.println("GridMasterGraphMaker.read: Error: " + exception.getMessage());
            exception.printStackTrace();
            exit(1);
        }
    }
}
