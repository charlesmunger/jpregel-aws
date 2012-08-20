package api;

/**
 *
 * @author charlesmunger
 */
public interface ReservationService
{
    public MachineGroup reserveWorkers(String instanceType, int numberOfWorkers);
    public MachineGroup reserveMaster(String instanceType);
}
