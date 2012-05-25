package system.commands;

import jicosfoundation.Command;
import jicosfoundation.Proxy;
import jicosfoundation.ServiceImpl;
import system.ComputeOutput;
import system.Master;

/**
 *
 * @author Pete Cappello
 */
public class SuperStepComplete implements Command
{
//    private boolean thereIsANextStep;
    ComputeOutput computeOutput;
    
//    public SuperStepComplete( boolean thereIsANextStep ) { this.thereIsANextStep = thereIsANextStep; }
    public SuperStepComplete( ComputeOutput computeOutput ) { this.computeOutput = computeOutput; }

    @Override
    public void execute(Proxy proxy) 
    { 
//        System.out.println("SuperStepComplete.execute: about to send.");
        proxy.sendCommand( this ); 
//        System.out.println("SuperStepComplete.execute:  sent.");
    }

    @Override
    public void execute(ServiceImpl serviceImpl) throws Exception 
    {
//        System.out.println("SuperStepComplete.execute: entered.");
        Master master = (Master) serviceImpl;
        master.superStepComplete( computeOutput );
//        System.out.println("SuperStepComplete.execute: complete.");
    }
}
