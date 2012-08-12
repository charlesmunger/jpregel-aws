package system;

import api.MasterOutputMaker;
import JpAws.S3MasterOutputMaker;
import java.io.*;
import static java.lang.System.err;
import static java.lang.System.exit;

/**
 *
 * @author Peter Cappello
 */
public class MasterOutputMakerStandard implements MasterOutputMaker 
{
    @Override
    public void write(FileSystem fileSystem, int numWorkers) {

        // open Master file for output
        int fis_read = 0;
        BufferedReader bufferedReader = null;
        DataInputStream dataInputStream = null;
        FileInputStream fileInputStream = null;
        boolean isEc2 = fileSystem.getFileSystem();
        String jobDirectoryName = fileSystem.getJobDirectory();
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = fileSystem.getFileOutputStream();
        } catch (FileNotFoundException ex) {
            System.err.println("File not found: " + ex.getLocalizedMessage());
        }
        DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(dataOutputStream));
        try {
            for (int fileNum = 1; fileNum <= numWorkers; fileNum++) {
                if (isEc2) {
                    S3MasterOutputMaker masterOutputMaker = new S3MasterOutputMaker(fileNum);
                    bufferedReader = masterOutputMaker.FileInput(jobDirectoryName);
                } else {
                    // open Worker output file for input
                    fis_read = 1;
                    fileInputStream = fileSystem.getWorkerOutputFileInputStream(fileNum);
                    dataInputStream = new DataInputStream(fileInputStream);
                    bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));
                }

                for (String line; (line = bufferedReader.readLine()) != null;) {
                    // append line to output file
                    bufferedWriter.write(line);
                    bufferedWriter.newLine();
                }
                // close Worker output file
                if (fis_read == 1) {
                    dataInputStream.close();
                    fileInputStream.close();
                }
                bufferedReader.close();
            }
            // close Master output file
            bufferedWriter.close();
            dataOutputStream.close();
            fileOutputStream.close();

            if (isEc2) {
                S3MasterOutputMaker masterOutputMaker = new S3MasterOutputMaker();
                masterOutputMaker.UploadFilesOntoS3(jobDirectoryName);
            }
        } catch (Exception exception) {
            err.println("StandardMasterOutputMaker.write: Error: " + exception.getMessage());
            exit(1);
        }
    }
}