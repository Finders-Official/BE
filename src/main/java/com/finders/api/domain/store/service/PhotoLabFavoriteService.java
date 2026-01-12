package com.finders.api.domain.store.service;

import com.finders.api.domain.community.entity.FavoritePhotoLab;
import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.member.repository.MemberUserRepository;
import com.finders.api.domain.store.entity.PhotoLab;
import com.finders.api.domain.store.repository.FavoritePhotoLabRepository;
import com.finders.api.domain.store.repository.PhotoLabRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhotoLabFavoriteService {

    private final FavoritePhotoLabRepository favoritePhotoLabRepository;
    private final PhotoLabRepository photoLabRepository;
    private final MemberUserRepository memberUserRepository;

    @Transactional
    public void addFavorite(Long memberId, Long photoLabId) {
        log.info("[PhotoLabFavoriteService.addFavorite] memberId={}, photoLabId={}", memberId, photoLabId);

        if (favoritePhotoLabRepository.existsByMemberIdAndPhotoLabId(memberId, photoLabId)) {
            return;
        }

        MemberUser member = memberUserRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        PhotoLab photoLab = photoLabRepository.findById(photoLabId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        FavoritePhotoLab favorite = new FavoritePhotoLab(member, photoLab);
        favoritePhotoLabRepository.save(favorite);
    }

    @Transactional
    public void removeFavorite(Long memberId, Long photoLabId) {
        log.info("[PhotoLabFavoriteService.removeFavorite] memberId={}, photoLabId={}", memberId, photoLabId);

        if (!favoritePhotoLabRepository.existsByMemberIdAndPhotoLabId(memberId, photoLabId)) {
            return;
        }

        favoritePhotoLabRepository.deleteByMemberIdAndPhotoLabId(memberId, photoLabId);
    }
}
