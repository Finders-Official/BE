package com.finders.api.domain.store.entity;

import com.finders.api.global.entity.BaseTimeEntity;
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
                @Index(name = "idx_region_parent", columnList = "parent_region_id"),
                @Index(name = "uk_region_name_parent", columnList = "region_name, parent_region_id", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Region extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_region_id")
    private Region parentRegion;

    @Column(name = "region_name", nullable = false, length = 50)
    private String regionName;

    public Region(Region parentRegion, String regionName) {
        this.parentRegion = parentRegion;
        this.regionName = regionName;
    }
}
