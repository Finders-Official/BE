package com.finders.api.domain.community.entity;

import com.finders.api.domain.community.enums.CommunityStatus;
import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_user_id", nullable = false)
    private MemberUser memberUser;

    @Column(nullable = false, length = 1000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommunityStatus status = CommunityStatus.ACTIVE;

    @Builder
    private Comment(String content, Post post, MemberUser memberUser) {
        this.content = content;
        this.post = post;
        this.memberUser = memberUser;
        this.status = CommunityStatus.ACTIVE;
    }

    public static Comment toEntity(String content, Post post, MemberUser memberUser) {
        return Comment.builder()
                .content(content)
                .post(post)
                .memberUser(memberUser)
                .build();
    }

    public void softDelete() {
        this.status = CommunityStatus.DELETED;
    }
}
