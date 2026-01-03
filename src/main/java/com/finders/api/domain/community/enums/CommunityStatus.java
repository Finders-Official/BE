package com.finders.api.domain.community.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommunityStatus {
    ACTIVE("활성"),
    HIDDEN("비공개"),
    DELETED("삭제");

    private final String description;
}