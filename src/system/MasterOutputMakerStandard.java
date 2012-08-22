package system;

import api.MasterOutputMaker;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import static java.lang.System.err;
import static java.lang.System.exit;

/**
 * 
 * @author Charles Munger
 */
public class MasterOutputMakerStandard implements MasterOutputMaker 
{
    @Override
    public void write(FileSystem fileSystem, int numWorkers) {
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = fileSystem.getFileOutputStream();
        try {
            for (int fileNum = 1; fileNum <= numWorkers; fileNum++) {
                try {
                    bufferedReader = fileSystem.getWorkerOutputFileInputStream(fileNum);
                } catch(Exception e) {
                    System.out.println("Error getting input stream for file " +fileNum + " with message " + e.getLocalizedMessage());
                }

                for (String line; (line = bufferedReader.readLine()) != null;) {
                    bufferedWriter.write(line);
                    bufferedWriter.newLine();
                }
              
                bufferedReader.close();
            }
            bufferedWriter.close();
        } catch (Exception exception) {
            err.println("StandardMasterOutputMaker.write: Error: " + exception.getMessage());
            exit(1);
        }
    }
}