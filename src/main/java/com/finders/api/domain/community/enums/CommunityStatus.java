package com.finders.api.domain.community.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostStatus {
    ACTIVE("활성"),
    INACTIVE("비활성"),
    REMOVED("삭제");

    private final String description;
}