/*
 * Used in conjunction with GridMasterGraphMaker
 * 
 * It reads a file that has 1 line of the form:
 * N blockSize r c 
 * where 0 <= r, c < N.
 * 
 * A worker that receives this file should create the vertices and out edges for
 * the subgrid whose lower left vertex has coordinates is (r, c) and whose
 * upper right vertex has coordinates ( r + blockSize - 1, c + blockSize - 1 ).
 */
package system;

import api.WorkerGraphMaker;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.IOException;
import static java.lang.System.exit;
import static java.lang.System.out;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 *
 * @author Pete Cappello
 */
public class WorkerGraphMakerGrid implements WorkerGraphMaker
{
    @Override
    public int makeGraph( Worker worker ) 
    {
        int workerNum       = worker.getWorkerNum();
        Job job = worker.getJob();
        FileSystem fileSystem = job.getFileSystem();
        VertexImpl vertexFactory  = job.getVertexFactory();
        int numVertices = 0;
        
        try
        {
            BufferedReader bufferedReader = fileSystem.getWorkerInputFileInputStream( workerNum );
            // read file
            //            FileInputStream fileInputStream = fileSystem.getWorkerInputFileInputStream( workerNum );
            //            DataInputStream dataInputStream = new DataInputStream( fileInputStream );
            //            BufferedReader bufferedReader   = new BufferedReader(new InputStreamReader( dataInputStream ));
            
            String line = bufferedReader.readLine();
            out.println(" Worker: " + workerNum + " input line: " + line);
            
            // extract N, blockSize, row, col from line
            StringTokenizer stringTokenizer = new StringTokenizer( line );
            int N         = getToken( stringTokenizer );
            out.println("workerNum: " + workerNum + ", N: " + N );
            int blockSize = getToken( stringTokenizer );
            out.println("workerNum: " + workerNum + ", B: " + blockSize );
            int row       = getToken( stringTokenizer );
            out.println("workerNum: " + workerNum + ", R: " + row );
            int col       = getToken( stringTokenizer );            
            out.println("workerNum: " + workerNum + ", C: " + col );
                        
            // construct vertices & their out edges
            for ( int rowOffset = 0; rowOffset < blockSize; rowOffset++ )
            {
                for ( int colOffset = 0; colOffset < blockSize; colOffset++ )
                {
                    // make vertexId
                    float x = row + rowOffset;
                    float y = col + colOffset;
                    Point2D.Float vertexId = new Point2D.Float( x, y);
                    StringBuffer stringVertex = new StringBuffer();
                    stringVertex.append( x ).append( " ").append( y ).append( " ");

                    // make its 2 out edges
                    Map<Point2D.Float, Point2D.Float> outEdgeMap = new HashMap<Point2D.Float, Point2D.Float>();
                    Point2D.Float target;

                    if ( x < N - 1 )
                    {
                        // make an edge to the row above
                        x = row + rowOffset + 1;
                        y = col + colOffset;
                        target = new Point2D.Float( x, y );              
                        outEdgeMap.put( target, target );
                        stringVertex.append( x ).append( " ").append( y ).append( " ");
                    }

                    if ( y < N - 1 )
                    {
                        // make an edge to the column to the right
                        x = row + rowOffset;
                        y = col + colOffset + 1;
                        target = new Point2D.Float( x, y );
                        outEdgeMap.put( target, target );
                        stringVertex.append( x ).append( " ").append( y ).append( " ");
                    } 
                    VertexImpl vertex = new VertexShortestPathEuclidean( vertexId, outEdgeMap );
                    String vertexString = new String( stringVertex );
                    worker.addVertex( vertex, vertexString );
                }
            }
            bufferedReader.close(); 
        }
        catch ( Exception exception )
        {
            out.println( "GridWorkerGraphMaker.makeGraph: Error: " + exception.getMessage());
            exception.printStackTrace();
            exit( 1 );
        }
        return numVertices;
    }
    
    private int getToken( StringTokenizer stringTokenizer ) throws IOException
    {
        if ( ! stringTokenizer.hasMoreTokens() )
        {
            out.println( "GridWorkerGraphMaker.makeGraph: getToken: Empty lines are not allowed." );
            throw new IOException();
        }
        return Integer.parseInt( stringTokenizer.nextToken() );
    }
}
