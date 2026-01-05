package com.finders.api.domain.member.entity;


import com.finders.api.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member_address", indexes = {
        @Index(name = "idx_address_member", columnList = "member_id, is_default")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberAddress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "address_name", nullable = false, length = 50)
    private String addressName;

    @Column(name = "recipient_name", length = 50)
    private String recipientName;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false, length = 10)
    private String zipcode;

    @Column(nullable = false, length = 200)
    private String address;

    @Column(name = "address_detail", length = 100)
    private String addressDetail;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;
}
