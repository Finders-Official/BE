package com.finders.api.domain.photo.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RestorationTier {
    /** flux-kontext-apps/restore-image — 마스크 불필요, ~$0.04/image */
    // BASIC("기본 복원", 1, "restore-image"),

    /** cjwbw/supir-v0q — CVPR 2024 SOTA, ~$0.14/image */
    PREMIUM("프리미엄 복원", 1, "supir-v0q");

    /** microsoft/old-photos → supir 파이프라인, ~$0.16/image (미구현) */
    // PRO("프로 복원", 3, "old-photos-pipeline");

    private final String description;
    private final int tokenCost;
    private final String modelIdentifier;
}
