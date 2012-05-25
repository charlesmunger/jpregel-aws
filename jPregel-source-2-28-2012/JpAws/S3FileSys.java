package JpAws;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;

import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.security.AWSCredentials;
import org.jets3t.service.*;
import org.jets3t.service.model.*;
import org.jets3t.service.multithread.S3ServiceSimpleMulti;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.multithread.DownloadPackage;


public class S3FileSys {
	
	
	public FileInputStream getFileInputStream( String jobDirectoryName )
    {
        return null; // Varsha: replace with code 
    }
	
	public static BufferedReader FileInput(String jobDirectoryName) throws ServiceException
	{
		//System.out.println("entering the FileInput function") ; 
		S3Service s3Service = S3Authorization() ; 
		String bucketName = jobDirectoryName + "/input" ; 
		BufferedReader reader  = null ;
		try {
			S3Object objectComplete = s3Service.getObject(bucketName, "input");
			reader = new BufferedReader( new InputStreamReader(objectComplete.getDataInputStream()));
			} 
		catch (S3ServiceException e) {
			e.printStackTrace();
		}
		//System.out.println("exiting the FileInput function") ; 

	
		return reader ; 
	}
	
	public static void WorkerUploadFiles(String jobDirectoryName, String folderName, int fileNum) throws NoSuchAlgorithmException, IOException, S3ServiceException 
	{ 
		//System.out.println("entering the WorkerUploadFiles function") ; 
		String bucketName = jobDirectoryName+"/"+folderName ; 
		String fileName = bucketName + "/" + fileNum ;  
		File fileData = new File(fileName);
		S3Object fileObject = new S3Object(fileData);
		S3Service s3Service = S3Authorization() ; 
		fileObject = s3Service.putObject(bucketName, fileObject);
		//System.out.println("exiting the WorkerUploadFiles function") ; 
	}
	
	public static S3Service S3Authorization() 
	{
		S3Service s3Service = null ; 
		String awsAccessKey = "AKIAIEINGU5VPVEQ4DAA";
		String awsSecretKey = "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";
		AWSCredentials awsCredentials = new AWSCredentials(awsAccessKey, awsSecretKey); 
		
		try {
			s3Service = new RestS3Service(awsCredentials);
			} 
		
		catch (S3ServiceException e) {
			e.printStackTrace();
			}
		return s3Service ; 
		
	}
	
	public static BufferedReader WorkerFileOutput(String jobDirectoryName, int workerNum) throws ServiceException
	{
		//System.out.println("entering the WorkerFileOutput function") ; 
		S3Service s3Service = S3Authorization() ; 
		String workerNumber = Integer.toString(workerNum) ; 
		BufferedReader reader  = null ;
		String bucketName = jobDirectoryName + "/out" ; 
		try {
			S3Object objectComplete = s3Service.getObject(bucketName, workerNumber);
			reader = new BufferedReader( new InputStreamReader(objectComplete.getDataInputStream()));
			} 
		catch (S3ServiceException e) {
			e.printStackTrace();
		}
		//System.out.println("exiting the WorkerFileOutput function") ; 

	
		return reader ; 
	}
	
	public static BufferedReader WorkerFileInput(String jobDirectoryName, int workerNum) throws ServiceException
	{
		//System.out.println("entering the WorkerFileInput function") ; 
		S3Service s3Service = S3Authorization() ; 
		String workerNumber = Integer.toString(workerNum) ; 
		BufferedReader reader  = null ;
		String bucketName = jobDirectoryName + "/in" ; 
		try {
			S3Object objectComplete = s3Service.getObject(bucketName, workerNumber);
			reader = new BufferedReader( new InputStreamReader(objectComplete.getDataInputStream()));
			} 
		catch (S3ServiceException e) {
			e.printStackTrace();
		}
	
		//System.out.println("exiting the WorkerFileInput function") ; 

		return reader ; 
	}
	
	
	public static void OutputUploadFiles(String jobDirectoryName) throws NoSuchAlgorithmException, IOException, S3ServiceException 
	{ 
		//System.out.println("entering the OutputUploadFiles function") ; 
		String bucketName = jobDirectoryName+"/"+ "output" ; 
		String fileName = bucketName + "/" + "output" ;  
		File fileData = new File(fileName);
		S3Object fileObject = new S3Object(fileData);
		S3Service s3Service = S3Authorization() ; 
		fileObject = s3Service.putObject(bucketName, fileObject);
		//System.out.println("exiting the OutputUploadFiles function") ; 

		
	}
	
}
