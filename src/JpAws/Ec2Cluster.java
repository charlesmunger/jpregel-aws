package JpAws;

import static java.lang.System.exit;
import static java.lang.System.out;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import system.ClientToMaster;
import system.Cluster;
import system.Master;

/**
 *
 * @author Charles Munger
 */
public class Ec2Cluster extends Cluster
{
    private static final int PUBLIC_DOMAIN_NAME = 0;
    private static final int PRIVATE_DOMAIN_NAME = 1;
    private MasterMachines masterMachines;
    
    @Override
    public synchronized ClientToMaster start(int numWorkers) throws RemoteException
    {
        out.println("Ec2Cluster.start.");
        String[] domainNames = {null, null};
        masterMachines = new MasterMachines();
        try 
        {
            domainNames = masterMachines.start(numWorkers);
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        String url = "//" + domainNames[ PUBLIC_DOMAIN_NAME] + ":" + Master.PORT + "/" + Master.SERVICE_NAME;
        for (int i = 0;; i += 5000) 
        {
            try 
            {
                master = (ClientToMaster) Naming.lookup(url);
            } 
            catch (NotBoundException ex) 
            {
                if (i > 120000) {
                    out.println("Master not up in time. Aborting");
                    try 
                    {
                        masterMachines.Stop();
                    } 
                    catch (IOException ex1) 
                    {
                        out.println("Failed terminating Master instance. Check webUI for zombie instances.");
                    }
                    exit(1);
                }
                out.println("Master not up yet. Trying again in 5 seconds...");
                try 
                {
                    wait(5000);
                } 
                catch (InterruptedException ex1) 
                {
                    out.println("Waiting interrupted, trying again immediately");
                }
                continue;
            } 
            catch (RemoteException r) 
            {
                if (i > 120000) 
                {
                    out.println("Master not up in time. Aborting");
                    try 
                    {
                        masterMachines.Stop();
                    } 
                    catch (IOException ex1) 
                    {
                        out.println("Failed terminating Master instance. Check webUI for zombie instances.");
                    }
                    exit(1);
                }
                out.println("Master not up yet. Trying again in 5 seconds...");
                try 
                {
                    wait(5000);
                } 
                catch (InterruptedException ex1) 
                {
                    out.println("Waiting interrupted, trying again immediately");
                }
                continue;
            } 
            catch (MalformedURLException ex) 
            {
                out.println("Bad master URL. " + ex.getLocalizedMessage());
                try 
                {
                    masterMachines.Stop();
                } 
                catch (IOException unused) 
                {
                    out.println("Failed terminating Master instance. Check webUI for zombie instances.");
                }
                exit(1);
            }
            break;
        }
        
        master.makeWorkers(numWorkers, domainNames[ PRIVATE_DOMAIN_NAME]);
        return master;
    }
    
    @Override
    public void stop() 
    {
        try 
        {
            master.shutdown();
            out.println("Terminating Master instance");
            masterMachines.Stop();
        } 
        catch (IOException exception) 
        {
            exception.printStackTrace();
        }
    }
}
