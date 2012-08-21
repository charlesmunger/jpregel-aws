/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package api;

import java.io.IOException;

/**
 * This interface provides for separation between infrastructure and client software. Implementing
 * classes should specify the return type of the remote object.
 * @author charlesmunger
 */
public interface MachineGroup<T>
{
    public String getHostname();
    public T deploy(String... args) throws IOException;
    public void reset() throws IOException;
    public void terminate() throws IOException;;
}
