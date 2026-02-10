package com.finders.api.infra.replicate;

public sealed interface ReplicateModelInput
        permits RestoreImageInput, SupirInput {

    String modelVersion();
}
