/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package api;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * This interface provides for separation between infrastructure and client software. Implementing
 * classes should specify the return type of the remote object.
 * @author charlesmunger
 */
public abstract class MachineGroup<T> implements Serializable
{
    private transient ExecutorService exec = Executors.newCachedThreadPool();
    public abstract String getHostname();
    public Future<T> deploy(final String... args) throws IOException {
        return exec.submit(new Callable<T>() {

            @Override
            public T call() throws Exception
            {
                return syncDeploy(args);
            }
        });
    }
    public abstract void reset() throws IOException;
    public abstract void terminate() throws IOException;
    public abstract T syncDeploy(String... args);
    private Object readResolve() throws ObjectStreamException {
        this.exec = Executors.newCachedThreadPool();
        return this;
    } 
}
