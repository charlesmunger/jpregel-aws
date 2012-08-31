package JpAws;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import java.io.*;
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
        new File("input/").mkdirs();
        new File("output/").mkdirs();
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
        try
        {
            return new S3Writer("output");
        } catch (FileNotFoundException ex)
        {
            return null;
        }
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
        try
        {
            return new S3Writer("in/"+WorkerNum);
        } catch (FileNotFoundException ex)
        {
            return null;
        }
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
        try
        {
            return new S3Writer("out/" + WorkerNum);
        } catch (FileNotFoundException ex)
        {
            return null;
        }
    }

    private class S3Writer extends BufferedWriter
    {
        private final String path;
        S3Writer(String path) throws FileNotFoundException 
        {
            super(new OutputStreamWriter(new FileOutputStream(new File(path))));
            this.path = path;
        }

        @Override
        public void close() throws IOException
        {
            super.close();
            s3.putObject(jobDirectoryName, path, new File(path));
        }
    }
}