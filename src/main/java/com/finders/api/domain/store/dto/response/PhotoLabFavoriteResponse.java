package com.finders.api.domain.store.dto.response;

import org.springframework.data.domain.Slice;

import java.util.List;

public class PhotoLabFavoriteResponse {

    public record Status(
            Long photoLabId,
            boolean isFavorite
    ) {
    }

    public record SliceResponse(
            List<PhotoLabListResponse.Card> photoLabs,
            PageInfo pageInfo
    ) {
        public static SliceResponse of(Slice<PhotoLabListResponse.Card> slice) {
            return new SliceResponse(
                    slice.getContent(),
                    new PageInfo(
                            slice.getNumber(),
                            slice.getSize(),
                            slice.isLast()
                    )
            );
        }
    }

    public record PageInfo(
            int currentPage,
            int pageSize,
            boolean isLast
    ) {}
}
