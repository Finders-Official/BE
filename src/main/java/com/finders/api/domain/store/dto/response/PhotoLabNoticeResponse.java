package com.finders.api.domain.store.dto.response;

import com.finders.api.domain.store.enums.NoticeType;
import lombok.Builder;

public class PhotoLabNoticeResponse {

    @Builder
    public record Rolling(
            String photoLabName,
            String noticeTitle,
            NoticeType noticeType
    ) {
    }
}
