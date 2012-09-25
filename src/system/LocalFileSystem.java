package system;

import java.io.*;

/**
 * Uses a directory path on the local machine.
 * @author Charles Munger
 */
public class LocalFileSystem extends FileSystem
{

    public LocalFileSystem(String jobDirectoryName)
    {
        super(jobDirectoryName);
        new File(jobDirectoryName + "/in/").mkdirs();
        new File(jobDirectoryName + "/out/").mkdirs();
    }

    @Override
    public BufferedReader getFileInputStream() throws FileNotFoundException
    {
        return read(jobDirectoryName + "/input");
    }

    @Override
    public BufferedReader getWorkerInputFileInputStream(int WorkerNum) throws FileNotFoundException
    {
        return read(jobDirectoryName + "/in/" + WorkerNum);
    }

    @Override
    public BufferedWriter getWorkerInputFileOutputStream(int WorkerNum) 
    {
        return write(jobDirectoryName + "/in/" + WorkerNum);
    }

    @Override
    public BufferedReader getWorkerOutputFileInputStream(int WorkerNum) throws FileNotFoundException
    {
        return read(jobDirectoryName + "/out/" + WorkerNum);
    }

    @Override
    public BufferedWriter getWorkerOutputFileOutputStream(int WorkerNum) 
    {
        return write(jobDirectoryName + "/out/" + WorkerNum);
    }

    @Override
    public BufferedWriter getFileOutputStream() 
    {
        return write(jobDirectoryName + "/output");
    }
    
    private BufferedReader read(String path) throws FileNotFoundException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(path)));
    }
    
    private BufferedWriter write(String path) {
        try
        {
            return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path)));
        } catch (FileNotFoundException ex)
        {
            System.err.println("Couldn't find file " + path + " to write to");
            return null;
        }
    }
}
