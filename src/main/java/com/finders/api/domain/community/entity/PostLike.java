package com.finders.api.domain.community.entity;

import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "post_like")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLike extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberUser memberUser;

    @Builder
    private PostLike(Post post, MemberUser memberUser) {
        this.post = post;
        this.memberUser = memberUser;
    }

    public static PostLike create(Post post, MemberUser memberUser) {
        return PostLike.builder()
                .post(post)
                .memberUser(memberUser)
                .build();
    }
}