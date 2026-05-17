package com.itau.chargeaccount.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Value Object that represents the result of processing a charge.
 */
@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class ProcessingResult {
    private final String status;      // SUCCESS, FAILURE
    private final String rejectionReason;

    public static ProcessingResult success() {
        return new ProcessingResult("SUCCESS", null);
    }

    public static ProcessingResult failure(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Rejection reason cannot be null");
        }
        return new ProcessingResult("FAILURE", reason);
    }

    public boolean isSuccess() {
        return "SUCCESS".equals(this.status);
    }

    public boolean isFailure() {
        return "FAILURE".equals(this.status);
    }

    @Override
    public String toString() {
        return "ProcessingResult{" +
                "status='" + status + '\'' +
                ", rejectionReason='" + rejectionReason + '\'' +
                '}';
    }
}

