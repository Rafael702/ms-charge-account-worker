package com.itau.chargeaccount.domain.entity;

import com.itau.chargeaccount.domain.valueobject.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain Entity that represents a Charge to be processed.
 *
 * A charge is a debit or credit that will be applied to the account.
 * Must be validated against the account status before processing.
 */
@Getter
@EqualsAndHashCode(of = "chargeId")
public class Charge {
    private final ChargeId chargeId;
    private final AccountId accountId;
    private final ChargeType type;
    private final BigDecimal amount;
    private final LocalDateTime chargeDate;

    private ChargeStatus chargeStatus;
    private AccountStatus accountStatus;
    private ProcessingResult processingResult;
    private String rejectionReason;
    private LocalDateTime processingDate;

    /**
     * Factory method to create a new charge.
     */
    public static Charge create(
            ChargeId chargeId,
            AccountId accountId,
            ChargeType type,
            BigDecimal amount) {

        if (chargeId == null) {
            throw new IllegalArgumentException("ChargeId cannot be null");
        }
        if (accountId == null) {
            throw new IllegalArgumentException("AccountId cannot be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("ChargeType cannot be null");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        return new Charge(chargeId, accountId, type, amount, LocalDateTime.now());
    }

    private Charge(
            ChargeId chargeId,
            AccountId accountId,
            ChargeType type,
            BigDecimal amount,
            LocalDateTime chargeDate) {
        this.chargeId = chargeId;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.chargeDate = chargeDate;
        this.chargeStatus = ChargeStatus.PENDING_VALIDATION;
        this.processingDate = null;
    }

    /**
     * Marks the charge as validating the account.
     */
    public void startAccountValidation(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
        this.chargeStatus = ChargeStatus.VALIDATING_ACCOUNT;
    }

    /**
     * Processes the charge based on account status.
     * If the account is ACTIVE, the charge is processed successfully.
     * Otherwise, it is rejected.
     */
    public void process() {
        if (this.accountStatus == null) {
            this.processingResult = ProcessingResult.failure("Account status not defined");
            this.rejectionReason = "Account status not defined";
            this.chargeStatus = ChargeStatus.REJECTED;
            this.processingDate = LocalDateTime.now();
            return;
        }

        if (!this.accountStatus.canProcess()) {
            String reason = String.format("Account with status %s cannot process charges", this.accountStatus.value());
            this.processingResult = ProcessingResult.failure(reason);
            this.rejectionReason = reason;
            this.chargeStatus = ChargeStatus.REJECTED;
        } else {
            this.processingResult = ProcessingResult.success();
            this.chargeStatus = ChargeStatus.PROCESSED;
        }

        this.processingDate = LocalDateTime.now();
    }

    /**
     * Verifies if the charge was processed successfully.
     */
    public boolean wasProcessedSuccessfully() {
        return this.processingResult != null &&
               this.processingResult.isSuccess() &&
               this.chargeStatus == ChargeStatus.PROCESSED;
    }

    /**
     * Verifies if the charge was rejected.
     */
    public boolean wasRejected() {
        return this.chargeStatus == ChargeStatus.REJECTED;
    }

    @Override
    public String toString() {
        return "Charge{" +
                "chargeId=" + chargeId +
                ", accountId=" + accountId +
                ", type=" + type +
                ", amount=" + amount +
                ", chargeStatus=" + chargeStatus +
                '}';
    }
}

