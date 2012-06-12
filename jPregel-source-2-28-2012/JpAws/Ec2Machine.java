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
    
    @Override
    public void Stop() throws IOException {
        new Thread(new Runnable() {

            @Override
            public void run() {
                instanceGroup.terminate();
                System.out.println("Terminated machine.");
            }
        }).start();
    }
}
