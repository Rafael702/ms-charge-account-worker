package com.itau.chargeaccount.domain.valueobject;

/**
 * Value Object that represents the possible statuses of a Charge.
 */
public enum ChargeStatus {
    PENDING_VALIDATION("Pending Validation"),
    VALIDATING_ACCOUNT("Validating Account"),
    PROCESSED("Processed"),
    REJECTED("Rejected");

    private final String description;

    ChargeStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}

