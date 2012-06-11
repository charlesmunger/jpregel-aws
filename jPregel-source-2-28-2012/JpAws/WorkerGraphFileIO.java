package JpAws;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import org.jets3t.service.S3Service;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Object;

/**
 * This class is used for the construction of graphs on workers.
 * @author charlesmunger
 */
public class WorkerGraphFileIO implements S3FileSystem {

    private int workerNum;

    /**
     * Creates a new object, which will acquire input and output files for a worker.
     * @param workerNum The worker's ID number, used for the individual files in S3.
     */
    public WorkerGraphFileIO(int workerNum) {
        this.workerNum = workerNum;
    }

    /**
     * Fetch the job input files from S3 before starting computation.
     * @param jobDirectoryName The bucket to download from.
     * @return Returns a bufferedReader pointed at a local copy of the S3 object.
     */
    @Override
    public BufferedReader FileInput(String jobDirectoryName) {
        String workerNumber = Integer.toString(workerNum);
        BufferedReader reader = null;
        String bucketName = jobDirectoryName + "/in";
        try {
            S3Service s3Service = new RestS3Service(PregelAuthenticator.get());
            S3Object objectComplete = s3Service.getObject(bucketName, workerNumber);
            reader = new BufferedReader(new InputStreamReader(objectComplete.getDataInputStream()));
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        return reader;
    }

    /**
     * Once computation is complete, upload the intermediary results to S3.
     * @param jobDirectoryName Specifies the bucket name to store the results in.
     */
    @Override
    public void UploadFilesOntoS3(String jobDirectoryName) {
        String bucketName = jobDirectoryName + "/" + "out";
        String fileName = bucketName + "/" + workerNum;
        File fileData = new File(fileName);
        try {
            S3Service s3Service = new RestS3Service(PregelAuthenticator.get());
            s3Service.putObject(bucketName, new S3Object(fileData));
        } catch (ServiceException e) {
        } catch (IOException e) {
        } catch (NoSuchAlgorithmException e) {
        }
    }
}
