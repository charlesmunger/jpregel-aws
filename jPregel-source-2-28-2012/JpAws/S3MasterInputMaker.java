package JpAws;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class S3MasterInputMaker implements S3FileSystem {
    private int fileNum;
    private AmazonS3 s3 = new AmazonS3Client(PregelAuthenticator.get());
    public S3MasterInputMaker(int fileNum) {
        this.fileNum = fileNum;
    }

    public S3MasterInputMaker() {
    }

    @Override
    public BufferedReader FileInput(String jobDirectoryName) {
        String bucketName = jobDirectoryName + "/input";
        S3ObjectInputStream objectContent = s3.getObject(bucketName, "input").getObjectContent();
        return new BufferedReader(new InputStreamReader(objectContent));
    }

    @Override
    public void UploadFilesOntoS3(String jobDirectoryName) {
        String bucketName = jobDirectoryName + "/" + "in";
        String fileName = bucketName + "/" + fileNum;
        File fileData = new File(fileName);
        s3.putObject(bucketName, fileName, fileData);
    }
}
