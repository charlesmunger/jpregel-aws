package JpAws;

import java.io.BufferedReader;

abstract public class S3FileSystem {

	abstract public BufferedReader FileInput(String JobDirectoryName);
	
	abstract public void UploadFilesOntoS3(String JobDirectoryName) ; 

}