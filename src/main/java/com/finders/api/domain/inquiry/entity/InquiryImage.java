package com.finders.api.domain.inquiry.entity;

import com.finders.api.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inquiry_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false)
    private Inquiry inquiry;

    @Column(name = "object_path", nullable = false, length = 500)
    private String objectPath;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Builder
    private InquiryImage(Inquiry inquiry, String objectPath, Integer displayOrder) {
        this.inquiry = inquiry;
        this.objectPath = objectPath;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
    }

    public static InquiryImage create(Inquiry inquiry, String objectPath, Integer displayOrder) {
        return InquiryImage.builder()
                .inquiry(inquiry)
                .objectPath(objectPath)
                .displayOrder(displayOrder)
                .build();
    }
}
