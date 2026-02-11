package com.finders.api.domain.photo.enums;

public enum RestorationTier {
    BASIC("기본 복원", 1, "flux-kontext-pro");

    private final String description;
    private final int creditCost;
    private final String modelIdentifier;

    RestorationTier(String description, int creditCost, String modelIdentifier) {
        this.description = description;
        this.creditCost = creditCost;
        this.modelIdentifier = modelIdentifier;
    }

    public String getDescription() {
        return description;
    }

    public int getCreditCost() {
        return creditCost;
    }

    public String getModelIdentifier() {
        return modelIdentifier;
    }
}
