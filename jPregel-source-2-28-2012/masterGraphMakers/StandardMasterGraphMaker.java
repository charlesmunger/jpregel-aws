package masterGraphMakers;

import static java.lang.System.err;
import static java.lang.System.exit;
import static java.lang.System.out;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
            // make file
            FileInputStream fileInputStream = fileSystem.getFileInputStream();
            DataInputStream dataInputStream = new DataInputStream( fileInputStream );
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader( dataInputStream ) );
            String line;
            if ( ( line = bufferedReader.readLine() ) == null )
            {
                err.println( "SimpleFileReader: Error: input file has no lines." );
                exit( 1 );
            }
            int numV = Integer.parseInt( line );
            int fileNum;
            for ( fileNum = 1; fileNum <= numWorkers; fileNum++ )
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
            dataInputStream.close();
            fileInputStream.close();
        }
        catch ( Exception exception )
        {
            err.println( "SimpleFileReader.read: Error: " + exception.getMessage() );
            exit( 1 );
        }
    }
}
