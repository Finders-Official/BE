package com.finders.api.domain.store.dto.response;

import com.finders.api.domain.store.entity.PhotoLabNotice;
import com.finders.api.domain.store.enums.NoticeType;
import lombok.Builder;

import java.time.LocalDate;

public class PhotoLabNoticeResponse {

    @Builder
    public record Rolling(
            Long photoLabId,
            String photoLabName,
            String noticeTitle,
            NoticeType noticeType,
            LocalDate startDate,
            LocalDate endDate
    ) {
        public static Rolling from(PhotoLabNotice notice, String photoLabName) {
            return Rolling.builder()
                    .photoLabId(notice.getPhotoLab().getId())
                    .photoLabName(photoLabName)
                    .noticeTitle(notice.getTitle())
                    .noticeType(notice.getNoticeType())
                    .startDate(notice.getStartDate())
                    .endDate(notice.getEndDate())
                    .build();
        }
    }
}
