package edu.ucsb.jpregel.system;

import api.MasterGraphMaker;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import static java.lang.System.err;
import static java.lang.System.exit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Makes a complete binary tree with N nodes. If N != 2^m -1, for some natural
 * number m, then some leaves are missing. The shortest path from the root to a
 * deepest leaf is log_2 N.
 *
 * It produces files that have 1 line of the form: N blockSize v where 0 <= v <
 * N - 1 and blockSize = ceiling ( N / numWorkers )
 *
 * The worker reading the file creates vertices and out edges for vertices v, v
 * + 1, v + 2, ..., v + blockSize - 1, with their edges.
 *
 * @author Pete Cappello
 */
public class MasterGraphMakerBinaryTree2 implements MasterGraphMaker
{
    @Override
    public void make(FileSystem fileSystem, int numWorkers)
    {
        BufferedReader bufferedReader = null;
        try
        {
            bufferedReader = fileSystem.getFileInputStream();
        } catch (FileNotFoundException ex)
        {
            err.println("input file not found");
            exit(1);
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
            System.err.println("Error reading lines from file" + ex.getLocalizedMessage());
        }
        int numV = Integer.parseInt(line);
        for (int fileNum = 1; fileNum <= numWorkers; fileNum++)
        {
            // open file for output in "in" directory
            BufferedWriter bufferedWriter = fileSystem.getWorkerInputFileOutputStream(fileNum);

            // output line: startVertexId stopVertexId
            StringBuilder string = new StringBuilder();
            string.append(numV).append(' ');
            string.append(numWorkers);
            System.out.println("BinaryTreeMasterGraphMaker2.make: worker: " + fileNum + " " + string);
            try
            {
                bufferedWriter.write( new String( string ) );
                bufferedWriter.newLine();
            } catch (IOException ex)
            {
                System.err.println("Error writing line to file: " + ex.getLocalizedMessage());
            }
            try
            {   // close worker input file
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
            System.err.println("Error closing bufferedReader: " + ex.getMessage());
        }
    }
}
