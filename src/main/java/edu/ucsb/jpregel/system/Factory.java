package edu.ucsb.jpregel.system;

/**
 *
 * @author Pete Cappello
 */
public interface Factory<T> 
{
    /*
     * @return an object of type T
     */
    T make();
}
