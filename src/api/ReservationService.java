/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
