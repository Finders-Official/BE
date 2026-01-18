package com.finders.api.domain.photo.dto;

/**
 * AI 복원 이미지 공유 응답
 * <p>
 * 커뮤니티("사진수다")에 게시글 작성 시 필요한 이미지 정보를 제공합니다.
 * objectPath만 반환하여 프로젝트 전체 이미지 저장 방식과 일관성을 유지합니다.
 */
public record ShareResponse(
        String objectPath,
        Integer width,
        Integer height
) {
    public static ShareResponse of(String objectPath, Integer width, Integer height) {
        return new ShareResponse(objectPath, width, height);
    }
}
