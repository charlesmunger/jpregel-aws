package system;

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
