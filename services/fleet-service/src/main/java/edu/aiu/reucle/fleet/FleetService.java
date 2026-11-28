package edu.aiu.reucle.fleet;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/** Application service for FR7-FR8. */
public final class FleetService {
    public enum VehicleStatus { AVAILABLE, IN_USE, MAINTENANCE, RETIRED }
    public enum DriverShiftStatus { SCHEDULED, ACTIVE, COMPLETED }

    public static final class Vehicle {
        private final String vehicleId; private final String plateNumber; private final double capacityKg;
        private VehicleStatus status; private double mileageKm;
        public Vehicle(String vehicleId, String plateNumber, double capacityKg, VehicleStatus status, double mileageKm) {
            this.vehicleId=Objects.requireNonNull(vehicleId); this.plateNumber=Objects.requireNonNull(plateNumber);
            this.capacityKg=capacityKg; this.status=Objects.requireNonNull(status); this.mileageKm=mileageKm;
        }
        public void transitionTo(VehicleStatus next) {
            boolean allowed=(status==VehicleStatus.AVAILABLE && (next==VehicleStatus.IN_USE||next==VehicleStatus.MAINTENANCE||next==VehicleStatus.RETIRED))
                    ||(status==VehicleStatus.IN_USE && (next==VehicleStatus.AVAILABLE||next==VehicleStatus.MAINTENANCE))
                    ||(status==VehicleStatus.MAINTENANCE && (next==VehicleStatus.AVAILABLE||next==VehicleStatus.RETIRED));
            if(!allowed) throw new InvalidVehicleTransitionException(status,next); status=next;
        }
        public void recordMaintenance(double mileageKm) {
            if(mileageKm<this.mileageKm) throw new IllegalArgumentException("maintenance mileage cannot decrease");
            if(status==VehicleStatus.RETIRED) throw new InvalidVehicleTransitionException(status,VehicleStatus.MAINTENANCE);
            this.mileageKm=mileageKm; if(status!=VehicleStatus.MAINTENANCE) transitionTo(VehicleStatus.MAINTENANCE);
        }
        public String vehicleId(){return vehicleId;} public String plateNumber(){return plateNumber;}
        public double capacityKg(){return capacityKg;} public VehicleStatus status(){return status;} public double mileageKm(){return mileageKm;}
    }

    public static final class DriverShift {
        private final String shiftId, driverId, vehicleId; private final LocalDateTime startTime, endTime;
        private DriverShiftStatus status;
        public DriverShift(String shiftId,String driverId,String vehicleId,LocalDateTime startTime,LocalDateTime endTime,DriverShiftStatus status){
            this.shiftId=Objects.requireNonNull(shiftId);this.driverId=Objects.requireNonNull(driverId);this.vehicleId=Objects.requireNonNull(vehicleId);
            this.startTime=Objects.requireNonNull(startTime);this.endTime=Objects.requireNonNull(endTime);this.status=Objects.requireNonNull(status);
        }
        public void transitionTo(DriverShiftStatus next){
            boolean allowed=(status==DriverShiftStatus.SCHEDULED&&next==DriverShiftStatus.ACTIVE)||(status==DriverShiftStatus.ACTIVE&&next==DriverShiftStatus.COMPLETED);
            if(!allowed) throw new InvalidDriverShiftTransitionException(status,next); status=next;
        }
        public String shiftId(){return shiftId;} public String driverId(){return driverId;} public String vehicleId(){return vehicleId;}
        public LocalDateTime startTime(){return startTime;} public LocalDateTime endTime(){return endTime;} public DriverShiftStatus status(){return status;}
    }

    public record CreateDriverShiftCommand(String driverId,String vehicleId,LocalDateTime startTime,LocalDateTime endTime){
        public CreateDriverShiftCommand{Objects.requireNonNull(driverId);Objects.requireNonNull(vehicleId);Objects.requireNonNull(startTime);Objects.requireNonNull(endTime);}
    }
    public interface IVehicleRepository { List<Vehicle> findAvailable(LocalDate date); void save(Vehicle vehicle); }
    public interface IShiftRepository {
        boolean findOverlaps(String driverId,LocalDateTime startTime,LocalDateTime endTime);
        Optional<DriverShift> findById(String shiftId); void save(DriverShift shift);
    }
    public static class ShiftOverlapException extends RuntimeException { public ShiftOverlapException(String driverId){super("Driver already has an overlapping shift: "+driverId);} }
    public static class VehicleUnavailableException extends RuntimeException { public VehicleUnavailableException(String vehicleId){super("Vehicle is not available: "+vehicleId);} }
    public static class InvalidShiftTimeException extends RuntimeException { public InvalidShiftTimeException(){super("Shift startTime must be before endTime");} }
    public static class DriverShiftNotFoundException extends RuntimeException { public DriverShiftNotFoundException(String shiftId){super("Shift not found: "+shiftId);} }
    public static class InvalidVehicleTransitionException extends RuntimeException { public InvalidVehicleTransitionException(VehicleStatus from,VehicleStatus to){super("Invalid vehicle transition: "+from+" -> "+to);} }
    public static class InvalidDriverShiftTransitionException extends RuntimeException { public InvalidDriverShiftTransitionException(DriverShiftStatus from,DriverShiftStatus to){super("Invalid driver shift transition: "+from+" -> "+to);} }

    private final IVehicleRepository vehicleRepository; private final IShiftRepository shiftRepository; private final Supplier<String> shiftIdSupplier;
    public FleetService(IVehicleRepository vehicleRepository,IShiftRepository shiftRepository,Supplier<String> shiftIdSupplier){
        this.vehicleRepository=Objects.requireNonNull(vehicleRepository);this.shiftRepository=Objects.requireNonNull(shiftRepository);this.shiftIdSupplier=Objects.requireNonNull(shiftIdSupplier);
    }
    public DriverShift createShift(CreateDriverShiftCommand command){
        if(!command.startTime().isBefore(command.endTime())) throw new InvalidShiftTimeException();
        if(shiftRepository.findOverlaps(command.driverId(),command.startTime(),command.endTime())) throw new ShiftOverlapException(command.driverId());
        List<Vehicle> available=vehicleRepository.findAvailable(command.startTime().toLocalDate());
        boolean found=available.stream().anyMatch(v->v.vehicleId().equals(command.vehicleId())&&v.status()==VehicleStatus.AVAILABLE);
        if(!found) throw new VehicleUnavailableException(command.vehicleId());
        DriverShift shift=new DriverShift(shiftIdSupplier.get(),command.driverId(),command.vehicleId(),command.startTime(),command.endTime(),DriverShiftStatus.SCHEDULED);
        shiftRepository.save(shift);return shift;
    }
    public DriverShift startShift(String shiftId){return transition(shiftId,DriverShiftStatus.ACTIVE);}
    public DriverShift endShift(String shiftId){return transition(shiftId,DriverShiftStatus.COMPLETED);}
    private DriverShift transition(String shiftId,DriverShiftStatus target){
        DriverShift shift=shiftRepository.findById(shiftId).orElseThrow(()->new DriverShiftNotFoundException(shiftId));
        shift.transitionTo(target);shiftRepository.save(shift);return shift;
    }
}
