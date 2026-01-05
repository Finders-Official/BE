package com.finders.api.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member_owner")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue("OWNER")
@PrimaryKeyJoinColumn(name = "member_id")
public class MemberOwner extends Member {

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "business_number", length = 20)
    private String businessNumber;

    @Column(name = "bank_name", length = 50)
    private String bankName;

    @Column(name = "bank_account_number", length = 50)
    private String bankAccountNumber;

    @Column(name = "bank_account_holder", length = 50)
    private String bankAccountHolder;

}
