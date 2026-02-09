package com.finders.api.domain.photo.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RestorationTier {
    /** FLUX.1 Fill [pro] - mask 기반 인페인팅, $0.05/image, 2토큰 */
    BASIC("기본 복원", 2, "flux-fill-pro");

    private final String description;
    private final int tokenCost;
    private final String modelIdentifier;
}
