package masterGraphMakers;

import static java.lang.System.err;
import static java.lang.System.exit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import JpAws.S3MasterInputMaker;
import system.FileSystem;
import system.MasterGraphMaker;

public class PageRankMasterGraphMaker implements MasterGraphMaker 
{
    @Override
    public void make(FileSystem fileSystem, int numWorkers) 
    {
        try
        {
            int fis_used = 0 ; 
            BufferedReader bufferedReader = null ; 
            DataInputStream dataInputStream =null ; 
            FileInputStream fileInputStream = null ;        	
            String jobDirectoryName = null ; 

            boolean isEc2 = fileSystem.getFileSystem() ; 
            if (isEc2) 
            {
                //System.out.println(" G1MasterGraphMaker.read() : jus entered isEc2") ;
                jobDirectoryName = fileSystem.getJobDirectory() ; 
                S3MasterInputMaker masterFileMaker = new S3MasterInputMaker () ; 
                bufferedReader = masterFileMaker.FileInput(jobDirectoryName) ;  
                //System.out.println(" G1MasterGraphMaker.read() : exiting isEc2" + jobDirectoryName) ;
            }
            else
            {
                // make file
                fis_used = 1 ; 
                fileInputStream = fileSystem.getFileInputStream();
                dataInputStream = new DataInputStream( fileInputStream );
                bufferedReader = new BufferedReader(new InputStreamReader( dataInputStream ) ); 
            } 
            String line;
            while ( ( line = bufferedReader.readLine() ) == null )
            {
                err.println( "WorkerFileWriter1: Error: input file has no lines." );
                exit( 1 );
            }
            int numV = Integer.parseInt( line ); 
            if(fis_used == 1 )
            {
                fileInputStream.close() ; 
                dataInputStream.close();
            }
            int vertexNum = 0;
            int fileNum;
            for ( fileNum = 1; fileNum <= numWorkers; fileNum++ )
            {
                // open file for output in "in" directory
                //System.out.println(" G1MasterGraphMaker.read() 2: jus entered isEc2") ;
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
                    // create line for vertex whose number is vertexNum
                    StringBuilder string = new StringBuilder();
                    int endVertexNum = vertexNum + linesPerFile ; 
                    string.append( vertexNum ).append( ' ' ).append(endVertexNum).append( ' ' ).append(numV) ; 

                    /*for ( int targetVertexNum = vertexNum + 1; targetVertexNum < numV; targetVertexNum++ )
                    {
                        string.append( targetVertexNum ).append( ' ' );
                        int value = ( targetVertexNum == vertexNum + 1 ) ? -1 : 1;
                        string.append( value ).append( ' ' );
                    } */
                    String lines = new String( string );

                    // append line to output file
                    bufferedWriter.write( lines );
                    bufferedWriter.newLine();
                    vertexNum++;
                }

                bufferedWriter.close();
                dataOutputStream.close();
                fileOutputStream.close(); 
                if(isEc2)
                { 
                    S3MasterInputMaker masterFileMaker = new S3MasterInputMaker (fileNum) ; 
                    masterFileMaker.UploadFilesOntoS3(jobDirectoryName) ; 
                    //System.out.println(" I have uploaded IN/ worker files the files onto S3 : G1MasterGraphMaker.read() " + jobDirectoryName + fileNum) ; 
                }  
                // close output file
            }   // for ending
        }
        catch ( Exception exception )
        {
            err.println( "G1MasterGraphMaker.read: Error:is happening here damm " + exception.getMessage() );
            exit( 1 );
        }
    }
}
