package com.finders.api.domain.store.dto.response;

import com.finders.api.domain.store.enums.NoticeType;
import lombok.Builder;

public class PhotoLabNoticeResponse {

    @Builder
    public record Rolling(
            Long photoLabId,
            String photoLabName,
            String noticeTitle,
            NoticeType noticeType
    ) {
    }
}
