package com.finders.api.infra.replicate;

public sealed interface ReplicateModelInput
        permits SupirInput {

    String modelVersion();
}
