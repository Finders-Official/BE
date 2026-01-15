package com.finders.api.domain.store.service.command;

import com.finders.api.domain.member.entity.FavoritePhotoLab;
import com.finders.api.domain.member.entity.MemberUser;
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

    @Override
    public PhotoLabFavoriteResponse.Status addFavorite(Long photoLabId, MemberUser memberUser) {
        validateMember(memberUser);

        PhotoLab photoLab = photoLabRepository.findById(photoLabId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        if (photoLabFavoriteRepository.existsByMember_IdAndPhotoLab_Id(memberUser.getId(), photoLabId)) {
            throw new CustomException(ErrorCode.CONFLICT);
        }

        photoLabFavoriteRepository.save(new FavoritePhotoLab(memberUser, photoLab));

        return PhotoLabFavoriteResponse.Status.builder()
                .photoLabId(photoLabId)
                .isFavorite(true)
                .build();
    }

    @Override
    public PhotoLabFavoriteResponse.Status removeFavorite(Long photoLabId, MemberUser memberUser) {
        validateMember(memberUser);

        FavoritePhotoLab favorite = photoLabFavoriteRepository
                .findByMember_IdAndPhotoLab_Id(memberUser.getId(), photoLabId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        photoLabFavoriteRepository.delete(favorite);

        return PhotoLabFavoriteResponse.Status.builder()
                .photoLabId(photoLabId)
                .isFavorite(false)
                .build();
    }

    private void validateMember(MemberUser memberUser) {
        if (memberUser == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
    }
}
