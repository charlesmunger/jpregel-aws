package JpAws;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import system.FileSystem;

/**
 * This filesystem uses Amazon's S3 service, storing files in a bucket specified
 * during construction.
 *
 * @author Charles Munger
 */
public class S3FileSystem extends FileSystem
{

    private AmazonS3 s3 = new AmazonS3Client(PregelAuthenticator.get());

    public S3FileSystem(String jobDirectoryName)
    {
        super(jobDirectoryName);
    }

    @Override
    public BufferedReader getFileInputStream() throws FileNotFoundException
    {
        S3ObjectInputStream objectContent = s3.getObject(jobDirectoryName, "input").getObjectContent();
        return new BufferedReader(new InputStreamReader(objectContent));
    }

    @Override
    public BufferedWriter getFileOutputStream()
    {
        return inToOut("output");
    }

    @Override
    public BufferedReader getWorkerInputFileInputStream(int WorkerNum) throws FileNotFoundException
    {
        String workerNumber = Integer.toString(WorkerNum);
        S3ObjectInputStream objectContent = s3.getObject(jobDirectoryName, "in/" + workerNumber).getObjectContent();
        return new BufferedReader(new InputStreamReader(objectContent));
    }

    @Override
    public BufferedWriter getWorkerInputFileOutputStream(int WorkerNum)
    {
        return inToOut("in/" + WorkerNum);
    }

    @Override
    public BufferedReader getWorkerOutputFileInputStream(int WorkerNum) throws FileNotFoundException
    {
        String workerNumber = Integer.toString(WorkerNum);
        String bucketName = jobDirectoryName;
        S3ObjectInputStream objectContent = s3.getObject(bucketName, "out/" + workerNumber).getObjectContent();
        return new BufferedReader(new InputStreamReader(objectContent));
    }

    @Override
    public BufferedWriter getWorkerOutputFileOutputStream(int WorkerNum)
    {
        return inToOut("out/" + WorkerNum);
    }

    private S3Writer inToOut(final String fileName)
    {
        final PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = null;
        try
        {
            out = new PipedOutputStream(in);
        } catch (IOException ex)
        {
            Logger.getLogger(S3FileSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
        Thread thread = new Thread(new Runnable()
        {

            @Override
            public void run()
            {
                s3.putObject(jobDirectoryName, fileName, in, null);
            }
        });
        thread.start();
        return new S3Writer(thread, new OutputStreamWriter(out));
    }

    class S3Writer extends BufferedWriter
    {

        private Thread S3threadt;

        S3Writer(Thread t, OutputStreamWriter o)
        {
            super(o);
            this.S3threadt = t;
        }

        @Override
        public void close() throws IOException
        {
            super.close();
            try
            {
                S3threadt.join();
            } catch (InterruptedException ex)
            {
                throw new IOException(ex);
            }
        }
    }
}