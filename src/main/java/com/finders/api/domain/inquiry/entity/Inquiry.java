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
