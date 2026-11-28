package com.reucle.material.model;

import java.util.Objects;

// PM2: material-service Class Diagram -> Material
public class Material {
    private final String materialId;
    private final String name;
    private final String category;
    private final double rewardRatePerKg;
    private boolean recyclable;

    public Material(String materialId, String name, String category, double rewardRatePerKg, boolean recyclable) {
        this.materialId = Objects.requireNonNull(materialId);
        this.name = Objects.requireNonNull(name);
        this.category = Objects.requireNonNull(category);
        if (rewardRatePerKg < 0) {
            throw new IllegalArgumentException("Reward rate cannot be negative");
        }
        this.rewardRatePerKg = rewardRatePerKg;
        this.recyclable = recyclable;
    }

    // PM2: material-service Class/State -> Material.updateRecyclable
    public void updateRecyclable(boolean recyclable) {
        this.recyclable = recyclable;
    }

    public String getMaterialId() { return materialId; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getRewardRatePerKg() { return rewardRatePerKg; }
    public boolean isRecyclable() { return recyclable; }
}
