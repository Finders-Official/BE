package com.finders.api.domain.member.entity;


import com.finders.api.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

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
    private MemberUser user;

    @Column(name = "address_name", nullable = false, length = 50)
    private String addressName;

    @Column(nullable = false, length = 10)
    private String zipcode;

    @Column(nullable = false, length = 200)
    private String address;

    @Column(name = "address_detail", length = 100)
    private String addressDetail;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Builder
    public MemberAddress(MemberUser user, String addressName, String zipcode,
                         String address, String addressDetail, boolean isDefault) {
        this.user = user;
        this.addressName = addressName;
        this.zipcode = zipcode;
        this.address = address;
        this.addressDetail = addressDetail;
        this.isDefault = isDefault;
    }

    public void updateIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}
