package com.finders.api.domain.community.entity;

import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "search_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberUser memberUser;

    @Column(nullable = false)
    @Size(max = 100)
    private String keyword;

    @Column(name = "object_path", nullable = true)
    private String objectPath;

    @Column(nullable = true)
    private Integer width;

    @Column(nullable = true)
    private Integer height;

    @Builder
    private SearchHistory(MemberUser memberUser, String keyword, String objectPath, Integer width, Integer height) {
        this.memberUser = memberUser;
        this.keyword = keyword;
        this.objectPath = objectPath;
        this.width = width;
        this.height = height;
    }

    public void updateSearchInfo(String objectPath, Integer width, Integer height) {
        this.objectPath = objectPath;
        this.width = width;
        this.height = height;
    }
}
