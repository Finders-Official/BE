package com.finders.api.domain.member.entity;

import com.finders.api.domain.store.entity.PhotoLab;
import com.finders.api.global.entity.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "favorite_photo_lab",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_favorite_member_lab", columnNames = {"member_id", "photo_lab_id"})
        },
        indexes = {
                @Index(name = "idx_favorite_member", columnList = "member_id"),
                @Index(name = "idx_favorite_lab", columnList = "photo_lab_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FavoritePhotoLab extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberUser member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_lab_id", nullable = false)
    private PhotoLab photoLab;

    public FavoritePhotoLab(MemberUser member, PhotoLab photoLab) {
        this.member = member;
        this.photoLab = photoLab;
    }
}
