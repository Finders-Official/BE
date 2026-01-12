package com.finders.api.domain.store.entity;

import com.finders.api.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "region",
        indexes = {
                @Index(name = "idx_region_sido", columnList = "sido"),
                @Index(name = "uk_region_sigungu_sido", columnList = "sigungu, sido", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Region extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sido")
    private Region sido;

    @Column(nullable = false, length = 50)
    private String sigungu;

    public Region(Region sido, String sigungu) {
        this.sido = sido;
        this.sigungu = sigungu;
    }
}
