package com.example.java_template.common.persistence.dto;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Base entity cho tất cả các entity trong hệ thống.
 *
 * <p>Tự động fill các cột audit {@code createdAt}, {@code createdBy},
 * {@code updatedAt}, {@code updatedBy} thông qua {@link AuditingEntityListener}.
 * Cần bật {@code @EnableJpaAuditing} trên Application class để hoạt động.</p>
 */
@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 100)
    String createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    String updatedBy;
}
