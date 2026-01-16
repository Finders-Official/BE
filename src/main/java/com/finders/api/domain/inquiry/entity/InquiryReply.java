package com.finders.api.domain.inquiry.entity;

import com.finders.api.domain.member.entity.Member;
import com.finders.api.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "inquiry_reply", indexes = {
        @Index(name = "idx_reply_inquiry", columnList = "inquiry_id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryReply extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false)
    private Inquiry inquiry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replier_id", nullable = false)
    private Member replier;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Builder
    private InquiryReply(Inquiry inquiry, Member replier, String content) {
        this.inquiry = inquiry;
        this.replier = replier;
        this.content = content;
    }

    public static InquiryReply create(Inquiry inquiry, Member replier, String content) {
        InquiryReply reply = InquiryReply.builder()
                .inquiry(inquiry)
                .replier(replier)
                .content(content)
                .build();
        inquiry.addReply(reply);
        return reply;
    }
}
