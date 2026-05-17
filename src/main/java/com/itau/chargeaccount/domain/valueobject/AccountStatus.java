package com.itau.chargeaccount.domain.valueobject;

/**
 * Value Object that represents the possible statuses of an Account.
 * Immutable with value-based equality.
 */
public record AccountStatus(String value) {
    // Factory methods for known states
    public static final AccountStatus ACTIVE = new AccountStatus("ACTIVE");
    public static final AccountStatus CANCELLED = new AccountStatus("CANCELLED");
    public static final AccountStatus LEGAL_HOLD = new AccountStatus("LEGAL_HOLD");

    /**
     * Checks if the account can process charges.
     * Only ACTIVE accounts can process.
     */
    public boolean canProcess() {
        return this.equals(ACTIVE);
    }

    /**
     * Checks if the account is blocked.
     */
    public boolean isBlocked() {
        return this.equals(LEGAL_HOLD) || this.equals(CANCELLED);
    }
}

