package com.finders.api.infra.replicate;

public sealed interface ReplicateModelInput
        permits KontextProInput {

    String modelVersion();

    default boolean isOfficialModel() {
        return false;
    }
}
