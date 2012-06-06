package JpAws;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Object;

/**
 * 
 * @author charlesmunger
 */
public class S3FileSys {

    /**
     * Returns the "input" file from the specified S3 bucket.
     * @param jobDirectoryName The S3 bucket containing the input file for this job.
     * @return Returns a reader pointed at a lcoal copy of the input file. 
     * @throws ServiceException 
     */
    public static BufferedReader FileInput(String jobDirectoryName) throws ServiceException {
        String bucketName = jobDirectoryName + "/input";
        BufferedReader reader = null;
        try {
            S3Service s3Service = new RestS3Service(PregelAuthenticator.get());
            S3Object objectComplete = s3Service.getObject(bucketName, "input");
            reader = new BufferedReader(new InputStreamReader(objectComplete.getDataInputStream()));
        } catch (S3ServiceException e) {
            e.printStackTrace();
        }
        return reader;
    }

    /**
     * Uploads the worker's output files to the designated S3 bucket.
     * @param jobDirectoryName The name of the S3 bucket to upload to.
     * @param folderName The name of the folder in the JobDirectoryName to upload to. 
     * @param fileNum the name of the file to upload - usually the worker's ID.
     * @throws NoSuchAlgorithmException This is thrown when passing null credentials, or with a bad JRE
     * @throws IOException Error reading from the local file system
     * @throws S3ServiceException Error connecting to S3
     */
    public static void WorkerUploadFiles(String jobDirectoryName, String folderName, int fileNum) throws NoSuchAlgorithmException, IOException, S3ServiceException {
        //System.out.println("entering the WorkerUploadFiles function") ; 
        String bucketName = jobDirectoryName + "/" + folderName;
        String fileName = bucketName + "/" + fileNum;
        File fileData = new File(fileName);
        S3Object fileObject = new S3Object(fileData);
        S3Service s3Service = new RestS3Service(PregelAuthenticator.get());
        s3Service.putObject(bucketName, fileObject);
    }

    /**
     * Outputs 
     * @param jobDirectoryName
     * @param workerNum
     * @return
     * @throws ServiceException
     */
    public static BufferedReader WorkerFileOutput(String jobDirectoryName, int workerNum) throws ServiceException {
        S3Service s3Service = new RestS3Service(PregelAuthenticator.get());
        String workerNumber = Integer.toString(workerNum);
        BufferedReader reader = null;
        String bucketName = jobDirectoryName + "/out";
        try {
            S3Object objectComplete = s3Service.getObject(bucketName, workerNumber);
            reader = new BufferedReader(new InputStreamReader(objectComplete.getDataInputStream()));
        } catch (S3ServiceException e) {
            e.printStackTrace();
        }
        return reader;
    }

    /**
     *  Fetches the output of a given worker from the designated S3 bucket. 
     * @param jobDirectoryName The S3 bucket to fetch from
     * @param workerNum The name of the file to fetch
     * @return Returns a reader poined at a local copy of the file 
     * @throws ServiceException Error connecting to S3, or file not found
     */
    public static BufferedReader WorkerFileInput(String jobDirectoryName, int workerNum) throws ServiceException {
        S3Service s3Service = new RestS3Service(PregelAuthenticator.get());
        String workerNumber = Integer.toString(workerNum);
        BufferedReader reader = null;
        String bucketName = jobDirectoryName + "/in";
        try {
            S3Object objectComplete = s3Service.getObject(bucketName, workerNumber);
            reader = new BufferedReader(new InputStreamReader(objectComplete.getDataInputStream()));
        } catch (S3ServiceException e) {
            e.printStackTrace();
        }
        return reader;
    }

    /**
     * Uploads the final output file to the designated S3 bucket
     * @param jobDirectoryName The S3 bucket to upload to
     * @throws NoSuchAlgorithmException Thrown when using null keys, or a bad classpath
     * @throws IOException Error writing to local file system
     * @throws S3ServiceException Error connecting to S3
     */
    public static void OutputUploadFiles(String jobDirectoryName) throws NoSuchAlgorithmException, IOException, S3ServiceException {
        String bucketName = jobDirectoryName + "/" + "output";
        String fileName = bucketName + "/" + "output";
        File fileData = new File(fileName);
        S3Object fileObject = new S3Object(fileData);
        S3Service s3Service = new RestS3Service(PregelAuthenticator.get());
        s3Service.putObject(bucketName, fileObject);
    }
}
