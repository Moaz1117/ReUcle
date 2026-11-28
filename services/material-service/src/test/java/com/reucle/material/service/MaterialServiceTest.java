package com.reucle.material.service;

import com.reucle.material.model.Material;
import com.reucle.material.repository.IMaterialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// PM2 Test Map: material-service Class & Sequence Diagrams
@ExtendWith(MockitoExtension.class)
class MaterialServiceTest {

    @Mock
    private IMaterialRepository repository;

    private MaterialService service;

    @BeforeEach
    void setUp() {
        service = new MaterialService(repository);
    }

    @Test
    @DisplayName("[Material Sequence: Evaluate Rate] Recyclable material -> multiplies weightKg by rewardRatePerKg")
    void calculateRewardPoints_recyclableMaterial_returnsCalculatedPoints() {
        Material pet = new Material("mat-1", "PET Plastic", "Plastic", 12.0, true);
        when(repository.findById("mat-1")).thenReturn(Optional.of(pet));

        double points = service.calculateRewardPoints("mat-1", 3.5);

        assertEquals(42.0, points);
    }

    @Test
    @DisplayName("[Material Sequence: Evaluate Rate] Non-recyclable material -> returns zero points")
    void calculateRewardPoints_nonRecyclableMaterial_returnsZero() {
        Material contaminated = new Material("mat-2", "Soiled Paper", "Paper", 5.0, false);
        when(repository.findById("mat-2")).thenReturn(Optional.of(contaminated));

        double points = service.calculateRewardPoints("mat-2", 4.0);

        assertEquals(0.0, points);
    }
}
