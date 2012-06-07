package masterGraphMakers;

import JpAws.S3MasterInputMaker;
import java.io.*;
import static java.lang.System.err;
import static java.lang.System.exit;
import java.util.logging.Level;
import java.util.logging.Logger;
import system.FileSystem;
import system.MasterGraphMaker;

/**
 *
 * @author Pete Cappello
 */
public class G1MasterGraphMaker implements MasterGraphMaker {

    /**
     *
     */
    @Override
    public void make(FileSystem fileSystem, int numWorkers) {
        int fis_used = 0;
        BufferedReader bufferedReader = null;
        DataInputStream dataInputStream = null;
        FileInputStream fileInputStream = null;
        String jobDirectoryName = null;

        boolean isEc2 = fileSystem.getFileSystem();
        if (isEc2) {
            //System.out.println(" G1MasterGraphMaker.read() : jus entered isEc2") ;
            jobDirectoryName = fileSystem.getJobDirectory();
            S3MasterInputMaker masterFileMaker = new S3MasterInputMaker();
            bufferedReader = masterFileMaker.FileInput(jobDirectoryName);
            //System.out.println(" G1MasterGraphMaker.read() : exiting isEc2" + jobDirectoryName) ;
        } else {
            // make file
            fis_used = 1;
            try {
                fileInputStream = fileSystem.getFileInputStream();
            } catch (FileNotFoundException ex) {
                System.out.println("Error getting local filesystem input stream: " + ex.getLocalizedMessage());
            }
            dataInputStream = new DataInputStream(fileInputStream);
            bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));
        }
        String line = null;
        try {
            while ((line = bufferedReader.readLine()) == null) {
                err.println("WorkerFileWriter1: Error: input file has no lines.");
                exit(1);
            }
        } catch (IOException ex) {
            System.out.println("Error reading lines from file" + ex.getLocalizedMessage());
        }
        int numV = Integer.parseInt(line);
        if (fis_used == 1) {
            try {
                fileInputStream.close();
                dataInputStream.close();
            } catch (IOException ex) {
                System.out.println("Error closing input streams"+ex.getLocalizedMessage());
            }
        }
        int vertexNum = 0;
        int fileNum;
        for (fileNum = 1; fileNum <= numWorkers; fileNum++) {
            // open file for output in "in" directory
            //System.out.println(" G1MasterGraphMaker.read() 2: jus entered isEc2") ;
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = fileSystem.getWorkerInputFileOutputStream(fileNum);
            } catch (FileNotFoundException ex) {
                System.out.println("Error getting output file stream: " + ex.getMessage());
                System.exit(1);
            }
            DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(dataOutputStream));

            int linesPerFile = numV / numWorkers;
            if (fileNum <= numV % numWorkers) {
                linesPerFile++;
            }

            for (int lineNum = 0; lineNum < linesPerFile; lineNum++) {
                // create line for vertex whose number is vertexNum
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append(vertexNum).append(' ');
                for (int targetVertexNum = vertexNum + 1; targetVertexNum < numV; targetVertexNum++) {
                    stringBuffer.append(targetVertexNum).append(' ');
                    int value = (targetVertexNum == vertexNum + 1) ? -1 : 1;
                    stringBuffer.append(value).append(' ');
                }
                String lines = new String(stringBuffer);
                try {
                    // append line to output file
                    bufferedWriter.write(lines);
                    bufferedWriter.newLine();
                } catch (IOException ex) {
                    System.out.println("Error writing lines to file: " + ex.getLocalizedMessage());
                }

                vertexNum++;
            }
            try {
                bufferedWriter.close();
                dataOutputStream.close();
                fileOutputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(G1MasterGraphMaker.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (isEc2) {
                S3MasterInputMaker masterFileMaker = new S3MasterInputMaker(fileNum);
                masterFileMaker.UploadFilesOntoS3(jobDirectoryName);
                //System.out.println(" I have uploaded IN/ worker files the files onto S3 : G1MasterGraphMaker.read() " + jobDirectoryName + fileNum) ; 
            }
            // close output file

        }   // for ending
        try {
            // System.out.println(" I print this one, G1Graphmaker.read()1 : " ) ; 
            bufferedReader.close();
            //System.out.println(" I print this one, G1Graphmaker.read()2 : " ) ; 
            //System.out.println(" I print this one, G1Graphmaker.read()3 : " ) ; 
            //System.out.println("exiting the make function of G1MasterGraphMaker") ;  
        } catch (IOException ex) {
            System.out.println("Error closing bufferedReader: " + ex.getMessage());
        }



    }
}