package com.reucle.material.repository;

import com.reucle.material.model.Material;
import java.util.List;
import java.util.Optional;

// PM2: material-service Class/Collaboration Diagram -> IMaterialRepository
public interface IMaterialRepository {
    Optional<Material> findById(String materialId);
    List<Material> findAll();
    Material save(Material material);
}
