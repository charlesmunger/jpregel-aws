//package JpLAN;
//
//import api.MachineGroup;
//import api.ReservationService;
//
///**
// *
// * @author charlesmunger
// */
//public class LANReservationService implements ReservationService{
//    
//    @Override
//    public MachineGroup reserveWorkers(String instanceType, int numberOfWorkers)
//    {
//        return new LANWorkerMachineGroup(numberOfWorkers);
//    }
//
//    @Override
//    public MachineGroup reserveMaster(String instanceType)
//    {
//        return new LANMasterMachineGroup();
//    }
//
//}
