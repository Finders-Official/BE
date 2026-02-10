package com.finders.api.infra.replicate;

public sealed interface ReplicateModelInput
        permits FluxFillInput, FluxKontextInput {

    String modelVersion();
}
