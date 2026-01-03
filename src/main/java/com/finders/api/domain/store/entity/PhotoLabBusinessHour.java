package com.finders.api.domain.store.entity;

import com.finders.api.domain.store.enums.DayOfWeek;
import com.finders.api.global.entity.BaseEntity;
import com.finders.api.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(
        name = "photo_lab_business_hour",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_lab_hour", columnNames = {"photo_lab_id", "day_of_week"})
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PhotoLabBusinessHour extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_lab_id", nullable = false)
    private PhotoLab photoLab;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 3)
    private DayOfWeek dayOfWeek;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    @Builder.Default
    @Column(name = "is_closed", nullable = false)
    private boolean isClosed = false;
}
