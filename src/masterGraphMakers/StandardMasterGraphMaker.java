package masterGraphMakers;

import JpAws.S3MasterInputMaker;
import java.io.*;
import static java.lang.System.err;
import static java.lang.System.exit;

import system.FileSystem;
import system.MasterGraphMaker;

/**
 * Currently, the make method does not do much error checking.
 *
 * @author Peter Cappello
 */
public class StandardMasterGraphMaker implements MasterGraphMaker
{
    @Override
    public void make( FileSystem fileSystem, int numWorkers )
    {
        try
        {
            // read master file
            FileInputStream fileInputStream = null;
            DataInputStream dataInputStream = null;
            BufferedReader bufferedReader   = null;
            
            boolean isEc2 = fileSystem.getFileSystem();
            if ( isEc2 ) 
            {
                String jobDirectoryName = fileSystem.getJobDirectory();
                S3MasterInputMaker masterFileMaker = new S3MasterInputMaker();
                bufferedReader = masterFileMaker.FileInput(jobDirectoryName);
            } 
            else // use local file system
            {
                fileInputStream = fileSystem.getFileInputStream();
                dataInputStream = new DataInputStream(fileInputStream);
                bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));
            }
            String line;
            if ( ( line = bufferedReader.readLine() ) == null )
            {
                err.println( "StandardMasterGraphMaker: Error: input file has no lines." );
                exit( 1 );
            }
            int numV = Integer.parseInt( line );
            
            // make worker input files
            for ( int fileNum = 1; fileNum <= numWorkers; fileNum++ )
            {
                // open file for output in "in" directory
                FileOutputStream fileOutputStream = fileSystem.getWorkerInputFileOutputStream( fileNum );
                DataOutputStream dataOutputStream = new DataOutputStream( fileOutputStream );
                BufferedWriter bufferedWriter = new BufferedWriter( new OutputStreamWriter( dataOutputStream ) );
                
                int linesPerFile = numV / numWorkers;
                if ( fileNum <= numV % numWorkers )
                {
                    linesPerFile++;
                }
                
                for ( int lineNum = 0; lineNum < linesPerFile; lineNum++ )
                {
                    line = bufferedReader.readLine();
                    if ( line == null )
                    {
                        err.println( "Error: Stated |V| = " + numV + " less than actual lines in file. " );
                        throw new IOException();
                    }
                    // append line to output file
                    bufferedWriter.write( line );
                    bufferedWriter.newLine();
                }
                // close output file
                bufferedWriter.close();
                dataOutputStream.close();
                fileOutputStream.close();
            }
            bufferedReader.close();
            if ( ! isEc2 ) 
            {
                fileInputStream.close();
                dataInputStream.close();
            }
        }
        catch ( Exception exception )
        {
            err.println( "StandardMasterGraphMaker.make: " + exception.getMessage() );
            exception.printStackTrace();
            exit( 1 );
        }
    }
}
