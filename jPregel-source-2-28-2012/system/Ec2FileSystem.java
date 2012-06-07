package system;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 *
 * @author Pete Cappello
 */
public class Ec2FileSystem extends FileSystem {

    Ec2FileSystem(String jobDirectoryName, boolean isEc2) {
        super(jobDirectoryName, isEc2);
    }

    @Override
    public FileInputStream getFileInputStream() throws FileNotFoundException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FileOutputStream getFileOutputStream() throws FileNotFoundException {
        (new File(jobDirectoryName+"/output/")).mkdirs();
        return new FileOutputStream(jobDirectoryName + "/" + "output/output");
    }

    @Override
    public FileInputStream getWorkerInputFileInputStream(int WorkerNum) throws FileNotFoundException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FileOutputStream getWorkerInputFileOutputStream(int WorkerNum) throws FileNotFoundException {
        (new File(jobDirectoryName + "/in/")).mkdirs();
        return new FileOutputStream(jobDirectoryName + "/in/" + WorkerNum);
    }

    @Override
    public FileInputStream getWorkerOutputFileInputStream(int WorkerNum) throws FileNotFoundException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FileOutputStream getWorkerOutputFileOutputStream(int WorkerNum) throws FileNotFoundException {
        (new File(jobDirectoryName + "/out/")).mkdirs();
        String fileName = jobDirectoryName + "/out/" + WorkerNum;
        return new FileOutputStream(fileName);
    }

    @Override
    public boolean getFileSystem() {
        return isEc2;
    }

    @Override
    public String getJobDirectory() {
        return jobDirectoryName;
    }
}