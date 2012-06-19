/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package JpAws;

import datameer.awstasks.aws.ec2.InstanceGroup;
import java.io.IOException;

/**
 *  Abstract class for shared EC2 machine functionality.
 * @author charlesmunger
 */
public abstract class Ec2Machine implements Machine {
    protected InstanceGroup instanceGroup;
    public static final String SECURITY_GROUP = "jpregelgroup";
    @Override
    public void Stop() throws IOException {
        instanceGroup.terminate();
    }
}
