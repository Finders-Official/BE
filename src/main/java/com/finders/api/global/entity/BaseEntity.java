package com.finders.api.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Soft Delete를 지원하는 Base Entity
 * - BaseTimeEntity를 상속하여 createdAt, updatedAt 포함
 * - deletedAt 필드로 Soft Delete 지원
 */
@Getter
@MappedSuperclass
public abstract class BaseEntity extends BaseTimeEntity {

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Soft Delete 수행
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 삭제 여부 확인
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * Soft Delete 복구
     */
    public void restore() {
        this.deletedAt = null;
    }
}
