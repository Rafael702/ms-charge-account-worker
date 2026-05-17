package com.itau.chargeaccount.domain.valueobject;

import lombok.*;

import java.util.UUID;

/**
 * Value Object that represents the unique ID of a Charge.
 */
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ChargeId {
    private String value;

    public static ChargeId generate() {
        return new ChargeId(UUID.randomUUID().toString());
    }

    public static ChargeId valueOf(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("ChargeId cannot be null or empty");
        }
        return new ChargeId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}

