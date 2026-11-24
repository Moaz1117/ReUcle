package com.reucle.bin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.reucle.bin.adapter.ISensorGW;
import com.reucle.bin.api.SensorEventListener;
import com.reucle.bin.domain.BinStatus;
import com.reucle.bin.domain.SensorReading;
import com.reucle.bin.domain.SmartBin;
import com.reucle.bin.domain.WasteComposition;
import com.reucle.bin.dto.BinRegistrationData;
import com.reucle.bin.dto.SensorData;
import com.reucle.bin.exceptions.BinNotFoundException;
import com.reucle.bin.exceptions.DeviceRegistrationException;
import com.reucle.bin.exceptions.InvalidBinStateTransitionException;
import com.reucle.bin.exceptions.InvalidReadingException;
import com.reucle.bin.repository.IBinRepository;
import com.reucle.bin.service.BinManager;

@ExtendWith(MockitoExtension.class)
class BinManagerTest {

    @Mock
    private ISensorGW sensorGateway;

    @Mock
    private IBinRepository binRepository;

    private BinManager binManager;
    private SensorEventListener sensorEventListener;

    private SmartBin bin42;
    private WasteComposition wc1;
    private SensorData data1;
    private SensorReading r1;
    private SensorReading r2;

    @BeforeEach
    void setUp() {
        binManager = new BinManager(binRepository, sensorGateway);
        sensorEventListener = new SensorEventListener(binManager);

        r1 = new SensorReading(Instant.parse("2026-11-24T05:40:00Z"), 74.0f);
        r2 = new SensorReading(Instant.parse("2026-11-24T06:40:00Z"), 82.5f);
        wc1 = new WasteComposition("PLASTIC", 3.2f);
        data1 = new SensorData("BIN-0042", 82.5f, wc1, Instant.parse("2026-11-24T06:40:00Z"));

        bin42 = new SmartBin("BIN-0042", "U-1001", "ReBin-S2", 74.0f, BinStatus.ACTIVE);
        bin42.addReading(r1);
    }

    @Test
    void processIncomingReading_whenFillAtOrAboveThreshold_marksNeedsCollectionAndSaves() {
        when(sensorGateway.receiveReading()).thenReturn(data1);
        when(binRepository.findById("BIN-0042")).thenReturn(bin42);

        sensorEventListener.onSensorEvent();

        assertEquals(82.5f, bin42.getCurrentFillLevel());
        assertEquals(BinStatus.NEEDS_COLLECTION, bin42.getStatus());
        assertEquals("PLASTIC", bin42.getComposition().getType());
        assertEquals(3.2f, bin42.getComposition().getEstimatedQuantity());

        InOrder order = inOrder(sensorGateway, binRepository);
        order.verify(sensorGateway).receiveReading();
        order.verify(binRepository).findById("BIN-0042");
        order.verify(binRepository).save(bin42);
    }

    @Test
    void processIncomingReading_whenBinMissing_throwsBinNotFoundException() {
        when(sensorGateway.receiveReading()).thenReturn(data1);
        when(binRepository.findById("BIN-0042")).thenReturn(null);

        assertThrows(BinNotFoundException.class, () -> binManager.processIncomingReading());
        verify(binRepository, never()).save(any());
    }

    @Test
    void processIncomingReading_whenFillBelow80_staysActive() {
        SensorData lowData = new SensorData("BIN-0042", 65.0f, wc1, Instant.now());
        when(sensorGateway.receiveReading()).thenReturn(lowData);
        when(binRepository.findById("BIN-0042")).thenReturn(bin42);

        binManager.processIncomingReading();

        assertEquals(BinStatus.ACTIVE, bin42.getStatus());
        assertFalse(bin42.checkThreshold(BinManager.COLLECTION_THRESHOLD));
        verify(binRepository).save(bin42);
    }

    @Test
    void queryAndRegistrationMethods_workAsExpected() {
        when(sensorGateway.registerDevice()).thenReturn(true);
        binManager.registerBin(new BinRegistrationData("BIN-0050", "U-1001", "ReBin-S2"));
        verify(binRepository).save(argThat(b -> b.getBinId().equals("BIN-0050") && b.getStatus() == BinStatus.ACTIVE));

        when(binRepository.findById("BIN-0042")).thenReturn(bin42);
        when(binRepository.findNeedingCollection(80.0f)).thenReturn(List.of(bin42));

        assertEquals(BinStatus.ACTIVE, binManager.getBinStatus("BIN-0042"));
        assertEquals(1, binManager.getBinsNeedingCollection().size());
        assertEquals(1, binManager.getFillHistory("BIN-0042").size());
        assertEquals(74.0f, binManager.getFillHistory("BIN-0042").get(0).getFillLevel());
        assertEquals(82.5f, r2.getFillLevel());

        when(binRepository.findById("MISSING")).thenReturn(null);
        assertThrows(BinNotFoundException.class, () -> binManager.getBinStatus("MISSING"));
        assertThrows(BinNotFoundException.class, () -> binManager.getFillHistory("MISSING"));
    }

    @Test
    void stateTransitions_validAndInvalid() {
        SmartBin unregistered = new SmartBin("BIN-0099", "U-1002", "ReBin-S1");
        assertEquals(BinStatus.UNREGISTERED, unregistered.getStatus());
        assertThrows(InvalidBinStateTransitionException.class, () -> unregistered.updateReading(20f, wc1));
        assertThrows(InvalidBinStateTransitionException.class, unregistered::markNeedsCollection);
        assertThrows(InvalidBinStateTransitionException.class, unregistered::resolveCollection);
        assertThrows(InvalidBinStateTransitionException.class, unregistered::markOffline);
        assertThrows(InvalidBinStateTransitionException.class, unregistered::markOnline);

        unregistered.activate();
        assertEquals(BinStatus.ACTIVE, unregistered.getStatus());
        assertThrows(InvalidBinStateTransitionException.class, unregistered::activate);
        assertThrows(InvalidBinStateTransitionException.class, unregistered::resolveCollection);
        assertThrows(InvalidBinStateTransitionException.class, unregistered::markOnline);

        assertThrows(InvalidReadingException.class, () -> bin42.updateReading(120f, wc1));
        assertThrows(InvalidReadingException.class, () -> bin42.updateReading(-5f, wc1));

        bin42.markNeedsCollection();
        assertEquals(BinStatus.NEEDS_COLLECTION, bin42.getStatus());
        assertThrows(InvalidBinStateTransitionException.class, bin42::activate);
        assertThrows(InvalidBinStateTransitionException.class, bin42::markOnline);

        bin42.resolveCollection();
        assertEquals(BinStatus.ACTIVE, bin42.getStatus());
        assertEquals(0.0f, bin42.getCurrentFillLevel());

        bin42.markOffline();
        assertEquals(BinStatus.OFFLINE, bin42.getStatus());
        assertThrows(InvalidBinStateTransitionException.class, bin42::activate);
        assertThrows(InvalidBinStateTransitionException.class, bin42::resolveCollection);

        bin42.markOnline();
        assertEquals(BinStatus.ACTIVE, bin42.getStatus());

        bin42.retire();
        assertEquals(BinStatus.RETIRED, bin42.getStatus());
        assertThrows(InvalidBinStateTransitionException.class, () -> bin42.updateReading(10f, wc1));
        assertThrows(InvalidBinStateTransitionException.class, bin42::retire);
        assertThrows(InvalidBinStateTransitionException.class, bin42::markOffline);
    }

    @Test
    void registerBin_whenGatewayFails_throwsDeviceRegistrationException() {
        when(sensorGateway.registerDevice()).thenReturn(false);
        assertThrows(DeviceRegistrationException.class, () ->
            binManager.registerBin(new BinRegistrationData("BIN-0077", "U-1001", "ReBin-S2"))
        );
    }
}
