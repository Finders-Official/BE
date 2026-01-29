package com.finders.api.domain.community.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.finders.api.domain.community.enums.CommunityStatus;
import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.store.entity.PhotoLab;
import com.finders.api.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "post")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberUser memberUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_lab_id")
    private PhotoLab photoLab;

    @Column(nullable = false)
    private boolean isSelfDeveloped = false; // 자가 현상 여부 기본값으로 false

    @Column(nullable = false, length = 30)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(length = 300)
    private String labReview;

    @Column(nullable = false)
    private Integer likeCount = 0;

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    @Column(nullable = false)
    private Integer commentCount = 0;

    public void increaseCommentCount() {
        this.commentCount++;
    }

    public void decreaseCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }

    @OneToMany(mappedBy = "post")
    @OrderBy("displayOrder ASC")
    @JsonIgnoreProperties("post")
    private List<PostImage> postImageList = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommunityStatus status = CommunityStatus.ACTIVE;

    @Builder
    private Post(MemberUser memberUser, PhotoLab photoLab, boolean isSelfDeveloped,
                 String title, String content, String labReview) {
        this.memberUser = memberUser;
        this.photoLab = photoLab;
        this.isSelfDeveloped = isSelfDeveloped;
        this.title = title;
        this.content = content;
        this.labReview = labReview;
        this.likeCount = 0;
        this.commentCount = 0;
        this.status = CommunityStatus.ACTIVE;
    }

    public static Post toEntity(com.finders.api.domain.community.dto.request.PostRequest.CreatePostDTO request,
                                MemberUser memberUser,
                                PhotoLab photoLab) {
        return Post.builder()
                .memberUser(memberUser)
                .photoLab(photoLab)
                .isSelfDeveloped(request.isSelfDeveloped())
                .title(request.title())
                .content(request.content())
                .labReview(request.reviewContent())
                .build();
    }

    public void softDelete() {
        this.status = CommunityStatus.DELETED;
    }
}