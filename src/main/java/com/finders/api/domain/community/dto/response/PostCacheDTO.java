package com.finders.api.domain.community.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostCacheDTO implements Serializable {
    private Long id;
    private String title;
    private Integer likeCount;
    private Integer commentCount;
    private String objectPath;
}
