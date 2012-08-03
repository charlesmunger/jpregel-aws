package JpAws;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class S3MasterOutputMaker implements S3FileSystem {

    private AmazonS3 s3 = new AmazonS3Client(PregelAuthenticator.get());
    private int workerNum;

    public S3MasterOutputMaker(int workerNum) {
        this.workerNum = workerNum;
    }

    public S3MasterOutputMaker() {
    }

    @Override
    public BufferedReader FileInput(String jobDirectoryName) {
        String workerNumber = Integer.toString(workerNum);
        String bucketName = jobDirectoryName;
        S3ObjectInputStream objectContent = s3.getObject(bucketName,"out/"+workerNumber).getObjectContent();
        return new BufferedReader(new InputStreamReader(objectContent));
    }

    @Override
    public void UploadFilesOntoS3(String jobDirectoryName) {
        String bucketName = jobDirectoryName;
        String fileName = bucketName + "/" + "output";
        File fileData = new File(fileName);
        s3.putObject(bucketName, "output", fileData);
    }
}