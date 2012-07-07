package system;

import java.awt.geom.Point2D;

/**
 * The WorkerJob class exists to reduce the class file size. 
 *
 * @author Pete Cappello
 */
public class WorkerJob extends Job
{
    private final GraphMaker workerGraphMaker;
    private final WorkerWriter workerWriter;
    private FileSystem fileSystem;
    
    public WorkerJob( Job job )
    { 
        super( job );
        workerWriter     = job.getWorkerWriter();
        workerGraphMaker = job.getWorkerGraphMaker();
        System.out.println("WorkerJob: vertexFactory type: " + vertexFactory.getClass() );
    }
    
    public FileSystem getFileSystem() { return fileSystem; }
    
    /*
     * @return the number of vertices that were constructed.
     */
    public int makeGraph( Worker worker ) { return workerGraphMaker.makeGraph( worker ); }
    
    public void makeOutputFile( Worker worker ) { workerWriter.write( fileSystem, worker ); }
    
    public int getPartId( Vertex vertex ) { return getPartId( vertex.getVertexId() ); }
    
    public int getPartId( Object vertexId ) 
    { 
        return vertexFactory.getPartId( vertexId, getNumParts() );
//    	if ( vertexId instanceof Integer )
//        {
//            return ((Integer) vertexId ) % getNumParts();
//        }
//        if ( vertexId instanceof Point2D.Float )
//        {
//            double x = ((Point2D.Float) vertexId).getX();
//            double y = ((Point2D.Float) vertexId).getY();
//            return ((int) (x * 1001001.0 + y*100.0 )) % getNumParts();
//        }
//        System.out.println("WorkerJob.getPartId: Invalid vertexId type.");
//        System.exit( 1 );
//        return 0;
    }
    
    void setFileSystem( FileSystem fileSystem ) { this.fileSystem = fileSystem; }
}
