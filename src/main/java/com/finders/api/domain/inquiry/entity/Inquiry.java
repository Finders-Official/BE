package com.finders.api.domain.inquiry.entity;

import com.finders.api.domain.inquiry.enums.InquiryStatus;
import com.finders.api.domain.member.entity.Member;
import com.finders.api.domain.store.entity.PhotoLab;
import com.finders.api.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "inquiry", indexes = {
        @Index(name = "idx_inquiry_member", columnList = "member_id, status"),
        @Index(name = "idx_inquiry_lab", columnList = "photo_lab_id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inquiry extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_lab_id")
    private PhotoLab photoLab;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InquiryStatus status;

    @OneToMany(mappedBy = "inquiry", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<InquiryReply> replies = new ArrayList<>();

    @OneToMany(mappedBy = "inquiry", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    @BatchSize(size = 10)
    private List<InquiryImage> images = new ArrayList<>();

    @Builder
    private Inquiry(Member member, PhotoLab photoLab, String title, String content) {
        this.member = member;
        this.photoLab = photoLab;
        this.title = title;
        this.content = content;
        this.status = InquiryStatus.PENDING;
    }

    public static Inquiry create(Member member, PhotoLab photoLab, String title, String content) {
        return Inquiry.builder()
                .member(member)
                .photoLab(photoLab)
                .title(title)
                .content(content)
                .build();
    }

    public void addReply(InquiryReply reply) {
        this.replies.add(reply);
        this.status = InquiryStatus.ANSWERED;
    }

    public void addImage(InquiryImage image) {
        this.images.add(image);
    }

    /**
     * 문의 이미지 추가
     *
     * @param imagePaths GCS objectPath 리스트 (예: "temp/123/abc.png")
     */
    public void addImages(List<String> imagePaths) {
        for (int i = 0; i < imagePaths.size(); i++) {
            this.images.add(InquiryImage.create(this, imagePaths.get(i), i));
        }
    }

    public void close() {
        this.status = InquiryStatus.CLOSED;
    }

    public boolean isServiceInquiry() {
        return this.photoLab == null;
    }

    public boolean isPhotoLabInquiry() {
        return this.photoLab != null;
    }
}
