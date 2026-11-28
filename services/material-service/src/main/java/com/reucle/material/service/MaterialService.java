package com.reucle.material.service;

import com.reucle.material.model.Material;
import com.reucle.material.repository.IMaterialRepository;
import java.util.List;
import java.util.NoSuchElementException;

// PM2: material-service Class/Collaboration/Sequence Diagram -> MaterialService
public class MaterialService {
    private final IMaterialRepository repository;

    public MaterialService(IMaterialRepository repository) {
        this.repository = repository;
    }

    // PM2: material-service Sequence (Register Material) -> MaterialService.registerMaterial
    public Material registerMaterial(Material material) {
        return repository.save(material);
    }

    public Material getMaterial(String materialId) {
        return repository.findById(materialId)
                .orElseThrow(() -> new NoSuchElementException("Material not found: " + materialId));
    }

    public List<Material> listMaterials() {
        return repository.findAll();
    }

    // PM2: material-service Sequence (Evaluate Reward Rate) -> MaterialService.calculateRewardPoints
    public double calculateRewardPoints(String materialId, double weightKg) {
        if (weightKg <= 0) {
            throw new IllegalArgumentException("Weight must be positive");
        }
        Material material = getMaterial(materialId);
        if (!material.isRecyclable()) {
            return 0.0;
        }
        return weightKg * material.getRewardRatePerKg();
    }
}
