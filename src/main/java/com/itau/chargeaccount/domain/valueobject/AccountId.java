package com.itau.chargeaccount.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Value Object that represents the unique ID of an Account.
 */
@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class AccountId {
    private final String value;

    public static AccountId valueOf(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("AccountId cannot be null or empty");
        }
        return new AccountId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}

