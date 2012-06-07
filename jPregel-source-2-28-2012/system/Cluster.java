package system;

import JpAws.Machine;
import static java.lang.System.out;
import JpAws.MasterMachines;


import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;


/**
 * start & stop a Master and set of Workers
 *
 * @author Pete Cappello
 */
public class Cluster {

    private static final int PUBLIC_DOMAIN_NAME = 0;
    private static final int PRIVATE_DOMAIN_NAME = 1;
    private ClientToMaster master;
    private String jobName = null;
    public synchronized ClientToMaster start(boolean isEc2, int numWorkers, String jobName) throws RemoteException {
        String[] domainNames = {null, null};
        this.jobName = jobName;
        if (isEc2) {
            try {
                MasterMachines masterMachines = new MasterMachines();
                domainNames = masterMachines.start(numWorkers, Machine.AMIID);

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                wait(80000); // wait for Master to start
            } catch (InterruptedException ignore) {
            }
            String url = "//" + domainNames[ PUBLIC_DOMAIN_NAME] + ":" + Master.PORT + "/" + Master.SERVICE_NAME;
            try {
                master = (ClientToMaster) Naming.lookup(url);
            } catch (Exception exception) {
                out.println("Exception: " + url + " -- " + exception.getMessage());
                exception.printStackTrace();;
                System.exit(1);
            }
        } else {
            //master = new LocalMaster();
            master = new LocalMaster(numWorkers);
            System.out.println("Cluster.start: workers constructed: " + numWorkers);
        }
        master.makeWorkers(numWorkers, domainNames[ PRIVATE_DOMAIN_NAME], Machine.AMIID);
        return master;
    }

    public void stop(boolean isEc2) {
        if (isEc2) {
            try {
                MasterMachines masterMachines = new MasterMachines();
                masterMachines.Stop();

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            try {
                master.shutdown();
            } catch (RemoteException ignore) {
            } // Expected behavior.
        }
    }
}