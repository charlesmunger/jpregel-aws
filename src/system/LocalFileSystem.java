package system;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 *
 * @author Pete Cappello
 */
public class LocalFileSystem extends FileSystem
{

    LocalFileSystem(String jobDirectoryName)
    {
        super(jobDirectoryName, false);
        new File(jobDirectoryName + "/in/").mkdirs();
        new File(jobDirectoryName + "/out/").mkdirs();
    }

    @Override
    public FileInputStream getFileInputStream() throws FileNotFoundException
    {
        return new FileInputStream(jobDirectoryName + "/input");
    }

    @Override
    public FileInputStream getWorkerInputFileInputStream(int WorkerNum) throws FileNotFoundException
    {
        return new FileInputStream(jobDirectoryName + "/in/" + WorkerNum);
    }

    @Override
    public FileOutputStream getWorkerInputFileOutputStream(int WorkerNum) throws FileNotFoundException
    {
        return new FileOutputStream(jobDirectoryName + "/in/" + WorkerNum);
    }

    @Override
    public FileInputStream getWorkerOutputFileInputStream(int WorkerNum) throws FileNotFoundException
    {
        String fileName = jobDirectoryName + "/out/" + WorkerNum;
        return new FileInputStream(fileName);
    }

    @Override
    public FileOutputStream getWorkerOutputFileOutputStream(int WorkerNum) throws FileNotFoundException
    {
        String fileName = jobDirectoryName + "/out/" + WorkerNum;
        return new FileOutputStream(fileName);
    }

    @Override
    public FileOutputStream getFileOutputStream() throws FileNotFoundException
    {
        return new FileOutputStream(jobDirectoryName + "/output");
    }

    @Override
    public boolean getFileSystem()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getJobDirectory()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
