package com.itau.chargeaccount.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity that represents a Charge in the database.
 * Maps domain Charge aggregate to relational database.
 */
@Entity
@Table(name = "charge", indexes = {
    @Index(name = "idx_charge_id", columnList = "charge_id", unique = true),
    @Index(name = "idx_account_id", columnList = "account_id"),
    @Index(name = "idx_charge_status", columnList = "charge_status"),
    @Index(name = "idx_processing_date", columnList = "processing_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChargeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "charge_id", nullable = false, unique = true, length = 36)
    private String chargeId;

    @Column(name = "account_id", nullable = false, length = 20)
    private String accountId;

    @Column(name = "charge_type", nullable = false, length = 1)
    private String chargeType;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "charge_status", nullable = false, length = 25)
    private String chargeStatus;

    @Column(name = "account_status", length = 25)
    private String accountStatus;

    @Column(name = "processing_result", length = 10)
    private String processingResult;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "charge_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime chargeDate;

    @Column(name = "processing_date")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime processingDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

