/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package JpAws;

import java.io.*;

/**
 * 
 * @author charlesmunger
 */
public class PregelAuthenticator extends org.jets3t.service.security.AWSCredentials implements com.amazonaws.auth.AWSCredentials {

    private static PregelAuthenticator singleton = null;
    
    private static PregelAuthenticator load(File f) {
        BufferedReader ir = null;
        try {
            ir = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
        } catch (IOException ex) {
            System.err.println("Credentials file " + f.getAbsolutePath() + " not found.");
            System.exit(1);
        }
        String temp1 = null,temp2 = null;
        try {
            temp1 = ir.readLine();
            temp2 = ir.readLine();
        } catch (IOException ex) {
            System.err.println("Improper key file. Key files must contain the access key on the first line, and the secret key on the second.");
            System.exit(1);
        }
        return new PregelAuthenticator(temp1,temp2);
    }
    
    private PregelAuthenticator(String AccessKeyID, String SecretKeyID) {
        super(AccessKeyID,SecretKeyID);
    }

    @Override
    public String getAWSAccessKeyId() {
        System.out.println(super.getAccessKey());
        return super.getAccessKey();
    }

    @Override
    public String getAWSSecretKey() {
        return super.getSecretKey();
    }

    public static PregelAuthenticator get() {
        update();
        return singleton;
    }

    private static void update() {
        if (singleton == null) {
            singleton = load(new File("key.AWSkey"));
        }
    }
    
    @Override
    public Object clone() {
        throw new UnsupportedOperationException("Cannot be Cloned");
    }
}
