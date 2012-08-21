/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package JpAws;

import com.amazonaws.auth.BasicAWSCredentials;
import java.io.*;
import org.apache.http.annotation.Immutable;
/**
 * This class provides a single access point for all credentials and keys used for accessing Amazon's 
 * services and instances created by the user. 
 * @author Charles Munger
 */
@Immutable
public class PregelAuthenticator extends BasicAWSCredentials {

    private static final PregelAuthenticator singleton = load(new File("key.AWSkey"));
    
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

    /**
     * Returns this system's authenticator. 
     * @return An object that can be passed on to the AWS SDK.
     */
    public static PregelAuthenticator get() {
        return singleton;
    }
    
    /**
     * This object is a singleton, and cannot be cloned., 
     * @return Throws an exception. 
     */
    @Override
    public Object clone() {
        throw new UnsupportedOperationException("Cannot be Cloned");
    }
    
    /**
     * Returns a pointer to the worker private key file, stored in privatekey.pem
     * @return The private key file. 
     */
    public static File getPrivateKey() {
        return new File("privatekey.pem");
    }
    
    /**
     * Returns the key used for SSH access to the Master instance.
     * @return the private key file, stored in masterkey.pem
     */
    public static File getMasterPrivateKey() {
        return new File("masterkey.pem");
    }
    
    /**
     * Returns the name of the Master instance's keypair, as created via the AWS webUI.
     * @return The name of the key pair. 
     */
    public static String getMasterPrivateKeyName() {
        return "masterkey";
    }
    
    /**
     * Returns the name of the Worker instances' keypair, as created via the AWS webUI.
     * @return The name of the key pair.
     */
    public static String getPrivateKeyName() {
        return "privatekey";
    }
}
