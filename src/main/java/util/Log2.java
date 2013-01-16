package util;

/**
 * Compute the floor of lg(n).
 *
 * @author Pete Cappello
 */
public class Log2
{
    /**
    * Compute the floor of lg(n).
    * See http://stackoverflow.com/questions/3305059/how-do-you-calculate-log-base-2-in-java-for-integers
    * 
    * @param n the number whose lg is to be extracted
    * @throws throws IllegalArgumentException when its argument is negative.
    * @return floor of lg(n)
    */
    public static int lg( int n )
    {
        if ( n > 0 )
        {
//            System.out.println("Log2.lg: n: " + n + "  Integer.numberOfLeadingZeros( n ): " + Integer.numberOfLeadingZeros( n )
//                    + "  lg(n): " + (31 - Integer.numberOfLeadingZeros( n )));
            return 31 - Integer.numberOfLeadingZeros( n );
        }
        throw new IllegalArgumentException();
    }
}
