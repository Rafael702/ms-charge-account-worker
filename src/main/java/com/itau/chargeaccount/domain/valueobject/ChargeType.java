package com.itau.chargeaccount.domain.valueobject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Value Object that represents the type of charge (debit or credit).
 */
@Getter
@RequiredArgsConstructor
public enum ChargeType {
    DEBIT("D", "Debit"),
    CREDIT("C", "Credit");

    private final String code;
    private final String description;

    public static ChargeType fromCode(String code) {
        for (ChargeType type : ChargeType.values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid charge type: " + code);
    }

    @Override
    public String toString() {
        return description;
    }
}

