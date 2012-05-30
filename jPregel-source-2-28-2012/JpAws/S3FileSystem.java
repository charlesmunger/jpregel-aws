package JpAws;

import java.io.BufferedReader;

interface S3FileSystem {

	public BufferedReader FileInput(String JobDirectoryName);
	
	public void UploadFilesOntoS3(String JobDirectoryName) ; 

}