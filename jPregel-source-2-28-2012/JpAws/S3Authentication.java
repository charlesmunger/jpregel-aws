package JpAws;

import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.security.AWSCredentials;

public class S3Authentication {
	
	public static S3Service S3Identification() 
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

}
