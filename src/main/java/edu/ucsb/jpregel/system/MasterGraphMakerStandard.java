package edu.ucsb.jpregel.system;

import api.MasterGraphMaker;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import static java.lang.System.err;
import static java.lang.System.exit;

/**
 * Currently, the make method does not do much error checking.
 *
 * @author Peter Cappello
 */
public class MasterGraphMakerStandard implements MasterGraphMaker
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
                err.println("StandardMasterGraphMaker: Error: input file has no lines.");
                exit(1);
            }
            int numV = Integer.parseInt(line);

            // make worker input files
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
                    line = bufferedReader.readLine();
                    if (line == null)
                    {
                        err.println("Error: Stated |V| = " + numV + " less than actual lines in file. ");
                        throw new IOException();
                    }
                    // append line to output file
                    bufferedWriter.write(line);
                    bufferedWriter.newLine();
                }
                // close output file
                bufferedWriter.close();
            }
            bufferedReader.close();
        } catch (Exception exception)
        {
            err.println("StandardMasterGraphMaker.make: " + exception.getMessage());
            exception.printStackTrace();
            exit(1);
        }
    }
}
