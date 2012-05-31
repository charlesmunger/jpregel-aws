package JpAws;

import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;

public class S3Authentication {

    public static S3Service S3Identification() {
        S3Service s3Service = null;
        try {
            s3Service = new RestS3Service(PregelAuthenticator.get());
        } catch (S3ServiceException e) {
            e.printStackTrace();
        }
        return s3Service;
    }
}
