package JpAws;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;

import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.S3Service;
import org.jets3t.service.model.S3Object;

public class S3MasterOutputMaker extends S3FileSystem {

	private int workerNum ; 
	public S3MasterOutputMaker(int workerNum) 
	{
		this.workerNum = workerNum ; 
	}
	
	public S3MasterOutputMaker() { } 
	@Override
	public BufferedReader FileInput(String jobDirectoryName) {
		
		S3Service s3Service = S3Authentication.S3Identification() ;  
		String workerNumber = Integer.toString(workerNum) ; 
		BufferedReader reader  = null ;
		String bucketName = jobDirectoryName + "/out" ; 
		try {
			S3Object objectComplete = s3Service.getObject(bucketName, workerNumber);
			reader = new BufferedReader( new InputStreamReader(objectComplete.getDataInputStream()));
			} 
		catch (ServiceException e) {
			e.printStackTrace();
		}
		//System.out.println("exiting the WorkerFileOutput function") ; 

		return reader ; 		
	}

	@Override
	public void UploadFilesOntoS3(String jobDirectoryName) {
 
			String bucketName = jobDirectoryName+"/"+ "output" ; 
			String fileName = bucketName + "/" + "output" ;  
			File fileData = new File(fileName);
			try
			{
				S3Object fileObject = new S3Object(fileData);
				S3Service s3Service = S3Authentication.S3Identification() ; 
				fileObject = s3Service.putObject(bucketName, fileObject);
			} 
			 catch (S3ServiceException e) {
				e.printStackTrace();
			 }
			catch(IOException e )
			{
				e.printStackTrace();
			}
			catch(NoSuchAlgorithmException e)
			{
				e.printStackTrace();
			}

		}
}