package com.finders.api.domain.photo.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RestorationTier {
    /** flux-kontext-apps/restore-image — 마스크 불필요, $0.04/image */
    BASIC("기본 복원", 1, "restore-image"),

    /** cjwbw/supir-v0q — CVPR 2024 SOTA, $0.14/image */
    STANDARD("고급 복원", 3, "supir-v0q"),

    /** microsoft/old-photos → supir 파이프라인 (미구현) */
    PREMIUM("프리미엄 복원", 4, "old-photos-pipeline");

    private final String description;
    private final int tokenCost;
    private final String modelIdentifier;
}
