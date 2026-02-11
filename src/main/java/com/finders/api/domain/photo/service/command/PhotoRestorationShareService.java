package com.finders.api.domain.photo.service.command;

import com.finders.api.domain.photo.dto.response.ShareResponse;

/**
 * 사진 복원 공유 서비스 인터페이스
 * <p>
 * AI 복원 이미지를 커뮤니티에 공유하는 기능을 담당합니다.
 */
public interface PhotoRestorationShareService {

    /**
     * AI 복원 이미지를 커뮤니티 공유용으로 Public 버킷에 복사
     *
     * @param memberId      회원 ID
     * @param restorationId 복원 ID
     * @return objectPath, width, height 정보
     */
    ShareResponse shareToPublic(Long memberId, Long restorationId);
}
