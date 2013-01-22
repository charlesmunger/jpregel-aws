package JpLAN;

import api.MachineGroup;
import edu.ucsb.jpregel.system.ClientToMaster;
import edu.ucsb.jpregel.system.Master;
import java.io.IOException;
import java.rmi.Naming;
/**
 *
 * @author charlesmunger
 */
class LANMasterMachineGroup extends MachineGroup<ClientToMaster>
{

    public LANMasterMachineGroup()
    {
    }

    @Override
    public String getHostname()
    {
        return "127.0.0.1";
    }

    @Override
    public void reset() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void terminate() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ClientToMaster syncDeploy(String... args)
    {
//        new Thread(new Runnable()
//        {
//
//            @Override
//            public void run()
//            {
//                try
//                {
//                    System.out.println("Deploying Master");
//                    Runtime.getRuntime().exec("java -server -cp ./dist/jpregel-aws.jar:./dist/lib/*  -Djava.security.policy=policy JpLAN.LANMaster");
//                } catch (IOException ex)
//                {
//                    System.out.println("Failed to start java process." + ex.getLocalizedMessage());
//                }
//            }
//        }).start();
        String url = "//" + getHostname() + ":" + Master.PORT + "/" + Master.CLIENT_SERVICE_NAME;
        ClientToMaster remoteObject = null;
        for (int i = 0;; i += 300)
        {
            try
            {
                remoteObject = (ClientToMaster) Naming.lookup(url);
            } catch (Exception ex)
            {
                try
                {
                    Thread.sleep(300);
                } catch (InterruptedException ex1)
                {
                }
                continue;
            }
            break;
        }
        return remoteObject;
    }
}
