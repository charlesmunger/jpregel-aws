package JpLAN;

import api.MachineGroup;
import java.io.IOException;
import java.rmi.Naming;
import system.ClientToMaster;
import system.Master;

/**
 *
 * @author charlesmunger
 */
class LANMasterMachineGroup implements MachineGroup<ClientToMaster> {

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
    public ClientToMaster deploy(String... args) throws IOException
    {
        new Thread(new Runnable() {

            @Override
            public void run()
            {
//                try
//                {
//                    Runtime.getRuntime().exec("java -server -cp ./dist/jpregel-aws.jar:./dist/lib/*  -Djava.security.policy=policy JpLAN.LANMaster");
//                } catch (IOException ex)
//                {
//                    System.out.println("Failed to start java process." +ex.getLocalizedMessage());
//                }
            }
        }).start();
        String url = "//" + getHostname() + ":" + Master.PORT + "/" + Master.SERVICE_NAME;
        ClientToMaster remoteObject = null;
        for(int i  = 0;;i+=300) {
            try
            {
                 remoteObject = (ClientToMaster) Naming.lookup(url);
            } catch (Exception ex)
            {
                try
                {
                    Thread.sleep(300);
                } catch (InterruptedException ex1){}
                continue;
            }
            break;
        }
        return remoteObject;
    }

}
