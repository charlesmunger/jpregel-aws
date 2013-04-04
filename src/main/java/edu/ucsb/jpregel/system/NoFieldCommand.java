package edu.ucsb.jpregel.system;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import jicosfoundation.Command;
import jicosfoundation.ServiceImpl;

/**
 *
 * @author charlesmunger
 */
public abstract class NoFieldCommand<S extends ServiceImpl> implements Command<S>
{
    public NoFieldCommand() {}
    
    @Override
    public void writeExternal(ObjectOutput oo) throws IOException{}

    @Override
    public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException{}
}
