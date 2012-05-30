package JpAws;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;

import org.jets3t.service.S3Service;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.S3Object;

public class WorkerGraphFileIO implements S3FileSystem {

	private int workerNum ; 
	public WorkerGraphFileIO ( int workerNum)
	{
		this.workerNum = workerNum ; 
	}
	@Override
	public BufferedReader FileInput(String jobDirectoryName) {
		
		S3Service s3Service = S3Authentication.S3Identification() ;  
		String workerNumber = Integer.toString(workerNum) ; 
		BufferedReader reader  = null ;
		String bucketName = jobDirectoryName + "/in" ; 
		try {
			S3Object objectComplete = s3Service.getObject(bucketName, workerNumber);
			reader = new BufferedReader( new InputStreamReader(objectComplete.getDataInputStream()));
		}
			catch (ServiceException e) {
				e.printStackTrace();
			}
		return reader ; 
	}

	@Override
	public void UploadFilesOntoS3(String jobDirectoryName) {
		
		
		String bucketName = jobDirectoryName+"/"+"out" ; 
		String fileName = bucketName + "/" + workerNum ;  
		File fileData = new File(fileName);
		try
		{
		S3Object fileObject = new S3Object(fileData);
		S3Service s3Service = S3Authentication.S3Identification() ;  
		fileObject = s3Service.putObject(bucketName, fileObject);
		}
		catch(ServiceException e) 
		{
		} 
		catch(IOException e)
		{ 
		}
		catch(NoSuchAlgorithmException e) 
		{ 
			
		}
		
		
	}

}
