package com.reucle.bin.domain;

public class WasteComposition {
    private String type;
    private float estimatedQuantity;

    public WasteComposition(String type, float estimatedQuantity) {
        this.type = type;
        this.estimatedQuantity = estimatedQuantity;
    }

    public String getType() { return type; }
    public float getEstimatedQuantity() { return estimatedQuantity; }
}
