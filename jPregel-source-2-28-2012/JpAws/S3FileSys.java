package JpAws;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.S3Object;

public class S3FileSys {

    public FileInputStream getFileInputStream(String jobDirectoryName) {
        return null; //TODO Varsha: replace with code 
    }

    public static BufferedReader FileInput(String jobDirectoryName) throws ServiceException {
        //System.out.println("entering the FileInput function") ; 
        S3Service s3Service = S3Authentication.S3Identification();
        String bucketName = jobDirectoryName + "/input";
        BufferedReader reader = null;
        try {
            S3Object objectComplete = s3Service.getObject(bucketName, "input");
            reader = new BufferedReader(new InputStreamReader(objectComplete.getDataInputStream()));
        } catch (S3ServiceException e) {
            e.printStackTrace();
        }
        return reader;
    }

    public static void WorkerUploadFiles(String jobDirectoryName, String folderName, int fileNum) throws NoSuchAlgorithmException, IOException, S3ServiceException {
        //System.out.println("entering the WorkerUploadFiles function") ; 
        String bucketName = jobDirectoryName + "/" + folderName;
        String fileName = bucketName + "/" + fileNum;
        File fileData = new File(fileName);
        S3Object fileObject = new S3Object(fileData);
        S3Service s3Service = S3Authentication.S3Identification();
        fileObject = s3Service.putObject(bucketName, fileObject);
    }

    public static BufferedReader WorkerFileOutput(String jobDirectoryName, int workerNum) throws ServiceException {
        S3Service s3Service = S3Authentication.S3Identification();
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

    public static BufferedReader WorkerFileInput(String jobDirectoryName, int workerNum) throws ServiceException {
        S3Service s3Service = S3Authentication.S3Identification();
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

    public static void OutputUploadFiles(String jobDirectoryName) throws NoSuchAlgorithmException, IOException, S3ServiceException {
        String bucketName = jobDirectoryName + "/" + "output";
        String fileName = bucketName + "/" + "output";
        File fileData = new File(fileName);
        S3Object fileObject = new S3Object(fileData);
        S3Service s3Service = S3Authentication.S3Identification();
        s3Service.putObject(bucketName, fileObject);
    }
}
