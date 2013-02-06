package edu.ucsb.jpregel.system;

/**
 * Make an object of type T.
 * @author Pete Cappello
 */
public interface Factory<T> 
{
    /*
     * @return an object of type T
     */
    T make();
}
