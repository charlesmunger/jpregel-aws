package system;

import JpAws.MasterMachines;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
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
    private MasterMachines masterMachines = new MasterMachines();

    public synchronized ClientToMaster start(boolean isEc2, int numWorkers, String jobName) throws RemoteException {
        String[] domainNames = {null, null};
        if (isEc2) {
            try {
                domainNames = masterMachines.start(numWorkers);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            String url = "//" + domainNames[ PUBLIC_DOMAIN_NAME] + ":" + Master.PORT + "/" + Master.SERVICE_NAME;
            for (int i = 0;; i += 5000) {
                try {
                    master = (ClientToMaster) Naming.lookup(url);
                } catch (NotBoundException ex) {
                    if (i > 80000) {
                        System.out.println("Master not up in time. Aborting");
                        try {
                            masterMachines.Stop();
                        } catch (IOException ex1) {
                            System.out.println("Failed terminating Master instance. Check webUI for zombie instances.");
                        }
                        System.exit(1);
                    }
                    System.out.println("Master not up yet. Trying again in 5 seconds...");
                    try {
                        wait(5000);
                    } catch (InterruptedException ex1) {
                        System.out.println("Waiting interrupted, trying again immediately");
                    }
                    continue;
                } catch (MalformedURLException ex) {
                    System.out.println("Bad master URL. " + ex.getLocalizedMessage());
                    try {
                        masterMachines.Stop();
                    } catch (IOException ex1) {
                        System.out.println("Failed terminating Master instance. Check webUI for zombie instances.");
                    }
                    System.exit(1);
                }
                break;
            }
        } else {
            master = new LocalMaster(numWorkers);
            System.out.println("Cluster.start: workers constructed: " + numWorkers);
        }
        master.makeWorkers(numWorkers, domainNames[ PRIVATE_DOMAIN_NAME]);
        return master;
    }

    public void stop(boolean isEc2) {
        if (isEc2) {
            try {
                master.shutdown();
                System.out.println("Terminating Master instance");
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