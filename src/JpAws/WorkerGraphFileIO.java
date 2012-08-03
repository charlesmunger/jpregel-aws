package JpAws;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * This class is used for the construction of graphs on workers.
 *
 * @author charlesmunger
 */
public class WorkerGraphFileIO implements S3FileSystem {

    private AmazonS3 s3 = new AmazonS3Client(PregelAuthenticator.get());
    private int workerNum;

    /**
     * Creates a new object, which will acquire input and output files for a
     * worker.
     *
     * @param workerNum The worker's ID number, used for the individual files in
     * S3.
     */
    public WorkerGraphFileIO(int workerNum) {
        this.workerNum = workerNum;
    }

    /**
     * Fetch the job input files from S3 before starting computation.
     *
     * @param jobDirectoryName The bucket to download from.
     * @return Returns a bufferedReader pointed at a local copy of the S3
     * object.
     */
    @Override
    public BufferedReader FileInput(String jobDirectoryName) {
        String workerNumber = Integer.toString(workerNum);
        String bucketName = jobDirectoryName;
        S3ObjectInputStream objectContent = s3.getObject(bucketName,"in/"+workerNumber).getObjectContent();
        return new BufferedReader(new InputStreamReader(objectContent));
    }

    /**
     * Once computation is complete, upload the intermediary results to S3.
     *
     * @param jobDirectoryName Specifies the bucket name to store the results
     * in.
     */
    @Override
    public void UploadFilesOntoS3(String jobDirectoryName) {
        String bucketName = jobDirectoryName;
        String fileName = bucketName + "/" + "out" + "/" + workerNum;
        File fileData = new File(fileName);
        s3.putObject(bucketName, "out" + "/" + workerNum, fileData);
    }
}
