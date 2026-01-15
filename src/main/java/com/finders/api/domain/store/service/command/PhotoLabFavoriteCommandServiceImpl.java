package com.finders.api.domain.store.service.command;

import com.finders.api.domain.member.entity.FavoritePhotoLab;
import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.member.repository.MemberUserRepository;
import com.finders.api.domain.store.dto.response.PhotoLabFavoriteResponse;
import com.finders.api.domain.store.entity.PhotoLab;
import com.finders.api.domain.store.repository.PhotoLabFavoriteRepository;
import com.finders.api.domain.store.repository.PhotoLabRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PhotoLabFavoriteCommandServiceImpl implements PhotoLabFavoriteCommandService {

    private final PhotoLabRepository photoLabRepository;
    private final PhotoLabFavoriteRepository photoLabFavoriteRepository;
    private final MemberUserRepository memberUserRepository;

    @Override
    public PhotoLabFavoriteResponse.Status addFavorite(Long photoLabId, Long memberId) {
        validateMember(memberId);

        PhotoLab photoLab = photoLabRepository.findById(photoLabId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        if (photoLabFavoriteRepository.existsByMember_IdAndPhotoLab_Id(memberId, photoLabId)) {
            throw new CustomException(ErrorCode.CONFLICT);
        }

        MemberUser memberUser = memberUserRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        photoLabFavoriteRepository.save(new FavoritePhotoLab(memberUser, photoLab));

        return PhotoLabFavoriteResponse.Status.builder()
                .photoLabId(photoLabId)
                .isFavorite(true)
                .build();
    }

    @Override
    public PhotoLabFavoriteResponse.Status removeFavorite(Long photoLabId, Long memberId) {
        validateMember(memberId);

        FavoritePhotoLab favorite = photoLabFavoriteRepository
                .findByMember_IdAndPhotoLab_Id(memberId, photoLabId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        photoLabFavoriteRepository.delete(favorite);

        return PhotoLabFavoriteResponse.Status.builder()
                .photoLabId(photoLabId)
                .isFavorite(false)
                .build();
    }

    private void validateMember(Long memberId) {
        if (memberId == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
    }
}
