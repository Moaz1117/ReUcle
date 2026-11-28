package edu.aiu.reucle.fleet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static edu.aiu.reucle.fleet.FleetService.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FleetServiceTest {
    static final LocalDate DAY=LocalDate.parse("2026-11-25");
    static final LocalDateTime START=LocalDateTime.parse("2026-11-25T09:00:00");
    static final LocalDateTime END=LocalDateTime.parse("2026-11-25T13:00:00");
    static final Vehicle VEHICLE=new Vehicle("VH-017","ABC-1234",4800.0,VehicleStatus.AVAILABLE,12150.0);

    IVehicleRepository vehicles; IShiftRepository shifts; FleetService service; List<DriverShift> saved;
    @BeforeEach void setUp(){
        vehicles=mock(IVehicleRepository.class);shifts=mock(IShiftRepository.class);saved=new ArrayList<>();
        doAnswer(inv->{saved.add(inv.getArgument(0));return null;}).when(shifts).save(any(DriverShift.class));
        service=new FleetService(vehicles,shifts,()->"SHIFT-2026-0110");
    }

    @Test void createsScheduledShiftAfterCheckingOverlapAndVehicleAvailability(){
        when(shifts.findOverlaps("DR-104",START,END)).thenReturn(false);
        when(vehicles.findAvailable(DAY)).thenReturn(List.of(VEHICLE));
        DriverShift shift=service.createShift(new CreateDriverShiftCommand("DR-104","VH-017",START,END));
        assertEquals("SHIFT-2026-0110",shift.shiftId());assertEquals(DriverShiftStatus.SCHEDULED,shift.status());
        assertEquals("DR-104",shift.driverId());assertEquals("VH-017",shift.vehicleId());assertEquals(START,shift.startTime());assertEquals(END,shift.endTime());
        verify(shifts).findOverlaps("DR-104",START,END);verify(vehicles).findAvailable(DAY);verify(shifts).save(shift);
    }

    @Test void rejectsOverlappingShiftBeforeVehicleLookupOrSave(){
        when(shifts.findOverlaps("DR-104",START,END)).thenReturn(true);
        assertThrows(ShiftOverlapException.class,()->service.createShift(new CreateDriverShiftCommand("DR-104","VH-017",START,END)));
        verify(shifts).findOverlaps("DR-104",START,END);verifyNoInteractions(vehicles);verify(shifts,never()).save(any(DriverShift.class));
    }

    @Test void rejectsInvalidTimeRange(){
        assertThrows(InvalidShiftTimeException.class,()->service.createShift(new CreateDriverShiftCommand("DR-104","VH-017",END,START)));
        verifyNoInteractions(shifts,vehicles);
    }

    @Test void rejectsUnavailableVehicle(){
        when(shifts.findOverlaps("DR-104",START,END)).thenReturn(false);when(vehicles.findAvailable(DAY)).thenReturn(List.of());
        assertThrows(VehicleUnavailableException.class,()->service.createShift(new CreateDriverShiftCommand("DR-104","VH-017",START,END)));
        verify(shifts,never()).save(any(DriverShift.class));
    }

    @Test void startsThenCompletesTheSampleShift(){
        DriverShift sample=new DriverShift("SHIFT-2026-0110","DR-104","VH-017",START,END,DriverShiftStatus.SCHEDULED);
        when(shifts.findById(sample.shiftId())).thenReturn(Optional.of(sample));
        assertEquals(DriverShiftStatus.ACTIVE,service.startShift(sample.shiftId()).status());
        assertEquals(DriverShiftStatus.COMPLETED,service.endShift(sample.shiftId()).status());
        verify(shifts,times(2)).save(sample);
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> validVehicleTransitions(){
        return Stream.of(org.junit.jupiter.params.provider.Arguments.of(VehicleStatus.AVAILABLE,VehicleStatus.IN_USE),
                org.junit.jupiter.params.provider.Arguments.of(VehicleStatus.AVAILABLE,VehicleStatus.MAINTENANCE),
                org.junit.jupiter.params.provider.Arguments.of(VehicleStatus.AVAILABLE,VehicleStatus.RETIRED),
                org.junit.jupiter.params.provider.Arguments.of(VehicleStatus.IN_USE,VehicleStatus.AVAILABLE),
                org.junit.jupiter.params.provider.Arguments.of(VehicleStatus.IN_USE,VehicleStatus.MAINTENANCE),
                org.junit.jupiter.params.provider.Arguments.of(VehicleStatus.MAINTENANCE,VehicleStatus.AVAILABLE),
                org.junit.jupiter.params.provider.Arguments.of(VehicleStatus.MAINTENANCE,VehicleStatus.RETIRED));
    }
    static Stream<org.junit.jupiter.params.provider.Arguments> invalidVehicleTransitions(){
        List<org.junit.jupiter.params.provider.Arguments> args=new ArrayList<>();
        for(VehicleStatus from:VehicleStatus.values())for(VehicleStatus to:VehicleStatus.values())
            if(!((from==VehicleStatus.AVAILABLE&&(to==VehicleStatus.IN_USE||to==VehicleStatus.MAINTENANCE||to==VehicleStatus.RETIRED))
                    ||(from==VehicleStatus.IN_USE&&(to==VehicleStatus.AVAILABLE||to==VehicleStatus.MAINTENANCE))
                    ||(from==VehicleStatus.MAINTENANCE&&(to==VehicleStatus.AVAILABLE||to==VehicleStatus.RETIRED))))
                args.add(org.junit.jupiter.params.provider.Arguments.of(from,to));return args.stream();
    }
    @ParameterizedTest(name="allows vehicle {0} -> {1}") @MethodSource("validVehicleTransitions")
    void allowsEveryDocumentedVehicleTransition(VehicleStatus from,VehicleStatus to){
        Vehicle v=new Vehicle("VH-017","ABC-1234",4800,from,12150);assertDoesNotThrow(()->v.transitionTo(to));assertEquals(to,v.status());
    }
    @ParameterizedTest(name="rejects vehicle {0} -> {1}") @MethodSource("invalidVehicleTransitions")
    void rejectsEveryUndocumentedVehicleTransition(VehicleStatus from,VehicleStatus to){
        Vehicle v=new Vehicle("VH-017","ABC-1234",4800,from,12150);assertThrows(InvalidVehicleTransitionException.class,()->v.transitionTo(to));assertEquals(from,v.status());
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> validShiftTransitions(){
        return Stream.of(org.junit.jupiter.params.provider.Arguments.of(DriverShiftStatus.SCHEDULED,DriverShiftStatus.ACTIVE),
                org.junit.jupiter.params.provider.Arguments.of(DriverShiftStatus.ACTIVE,DriverShiftStatus.COMPLETED));
    }
    static Stream<org.junit.jupiter.params.provider.Arguments> invalidShiftTransitions(){
        List<org.junit.jupiter.params.provider.Arguments> args=new ArrayList<>();
        for(DriverShiftStatus from:DriverShiftStatus.values())for(DriverShiftStatus to:DriverShiftStatus.values())
            if(!((from==DriverShiftStatus.SCHEDULED&&to==DriverShiftStatus.ACTIVE)||(from==DriverShiftStatus.ACTIVE&&to==DriverShiftStatus.COMPLETED)))
                args.add(org.junit.jupiter.params.provider.Arguments.of(from,to));return args.stream();
    }
    @ParameterizedTest(name="allows shift {0} -> {1}") @MethodSource("validShiftTransitions")
    void allowsEveryDocumentedShiftTransition(DriverShiftStatus from,DriverShiftStatus to){
        DriverShift s=new DriverShift("SHIFT-2026-0110","DR-104","VH-017",START,END,from);assertDoesNotThrow(()->s.transitionTo(to));assertEquals(to,s.status());
    }
    @ParameterizedTest(name="rejects shift {0} -> {1}") @MethodSource("invalidShiftTransitions")
    void rejectsEveryUndocumentedShiftTransition(DriverShiftStatus from,DriverShiftStatus to){
        DriverShift s=new DriverShift("SHIFT-2026-0110","DR-104","VH-017",START,END,from);assertThrows(InvalidDriverShiftTransitionException.class,()->s.transitionTo(to));assertEquals(from,s.status());
    }
}
