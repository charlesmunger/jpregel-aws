package masterGraphMakers;

import static java.lang.System.err;
import static java.lang.System.exit;

import JpAws.S3MasterInputMaker;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import system.FileSystem;
import system.MasterGraphMaker;

/**
 * Makes a complete binary tree with N nodes.
 * If N != 2^m -1, for some natural number m, then some leaves are missing.
 * The shortest path from the root to a deepest leaf is log_2 N.
 * 
 * It produces files that have 1 line of the form:
 * N blockSize v  
 * where 0 <= v < N - 1 and blockSize = ceiling ( N / numWorkers )
 * 
 * The worker reading the file creates vertices and out edges for
 * vertices v, v + 1, v + 2, ..., v + blockSize - 1, with their edges.
 *
 * @author Pete Cappello
 */
public class BinaryTreeMasterGraphMaker implements MasterGraphMaker
{
    @Override
    public void make(FileSystem fileSystem, int numWorkers) 
    {
        BufferedReader bufferedReader = null;
        DataInputStream dataInputStream = null;
        FileInputStream fileInputStream = null;
        String jobDirectoryName = null;

        boolean isEc2 = fileSystem.getFileSystem();
        if ( isEc2 ) 
        {
            jobDirectoryName = fileSystem.getJobDirectory();
            S3MasterInputMaker masterFileMaker = new S3MasterInputMaker();
            bufferedReader = masterFileMaker.FileInput(jobDirectoryName);
        } 
        else // use local file system
        {
            try 
            {
                fileInputStream = fileSystem.getFileInputStream();
            } 
            catch (FileNotFoundException ex) 
            {
                System.err.println("Error getting local filesystem input stream: " + ex.getLocalizedMessage());
            }
            dataInputStream = new DataInputStream(fileInputStream);
            bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));
        }
        String line = null;
        try 
        {
            if ( ( line = bufferedReader.readLine() ) == null ) 
            {
                err.println("WorkerFileWriter1: Error: input file has no lines.");
                exit(1);
            }
        } 
        catch (IOException ex) 
        {
            System.err.println("Error reading lines from file" + ex.getLocalizedMessage());
        }
        int numV = Integer.parseInt(line);
        if ( ! isEc2 ) 
        {
            try 
            {
                fileInputStream.close();
                dataInputStream.close();
            } 
            catch (IOException ex) 
            {
                System.out.println("Error closing input streams"+ex.getLocalizedMessage());
            }
        }
//        int vertexNum = 1;
        for ( int vertexNum = 1, fileNum = 1; fileNum <= numWorkers; fileNum++) 
        {
            // open file for output in "in" directory
            FileOutputStream fileOutputStream = null;
            try 
            {
                fileOutputStream = fileSystem.getWorkerInputFileOutputStream(fileNum);
            } 
            catch (FileNotFoundException ex) 
            {
                System.err.println("Error getting output file stream: " + ex.getMessage());
                System.exit(1);
            }
            DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(dataOutputStream));

            int verticesPerFile = numV / numWorkers;
            if (fileNum <= numV % numWorkers) 
            {
                verticesPerFile++;
            }
            
            // output line: startVertexId stopVertexId
            StringBuilder string = new StringBuilder();
            string.append( vertexNum ).append(' ');
            vertexNum += ( verticesPerFile - 1 );
            string.append( vertexNum++ ).append(' ');
            string.append( numV );
            System.out.println("BinaryTreeMasterGraphMaker.make: worker: " + fileNum + " " + string );
            try 
            {
                bufferedWriter.write( new String(string) );
                bufferedWriter.newLine();
            } 
            catch (IOException ex) 
            {
                System.err.println("Error writing line to file: " + ex.getLocalizedMessage());
            }
            try 
            {   // close worker input file
                bufferedWriter.close();
                dataOutputStream.close();
                fileOutputStream.close();
            } 
            catch (IOException ex) 
            {
                Logger.getLogger(G1MasterGraphMaker.class.getName()).log(Level.SEVERE, null, ex);
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
        } 
        catch (IOException ex) 
        {
            System.err.println("Error closing bufferedReader: " + ex.getMessage());
        }
    }
}
