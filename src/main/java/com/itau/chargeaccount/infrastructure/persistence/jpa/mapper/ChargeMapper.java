package com.itau.chargeaccount.infrastructure.persistence.jpa.mapper;

import com.itau.chargeaccount.domain.entity.Charge;
import com.itau.chargeaccount.domain.valueobject.*;
import com.itau.chargeaccount.infrastructure.persistence.jpa.entity.ChargeEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Charge domain entity and ChargeEntity JPA entity.
 * Handles bidirectional conversion respecting hexagonal architecture.
 */
@Component
public class ChargeMapper {

    /**
     * Convert domain Charge to JPA ChargeEntity
     */
    public ChargeEntity toEntity(Charge charge) {
        if (charge == null) {
            return null;
        }

        return ChargeEntity.builder()
            .chargeId(charge.getChargeId().getValue())
            .accountId(charge.getAccountId().getValue())
            .chargeType(charge.getType().getCode())
            .amount(charge.getAmount())
            .chargeStatus(charge.getChargeStatus().name())
            .accountStatus(charge.getAccountStatus() != null ? charge.getAccountStatus().value() : null)
            .processingResult(charge.getProcessingResult() != null ? charge.getProcessingResult().getStatus() : null)
            .rejectionReason(charge.getRejectionReason())
            .chargeDate(charge.getChargeDate())
            .processingDate(charge.getProcessingDate())
            .build();
    }

    /**
     * Convert JPA ChargeEntity to domain Charge
     */
    public Charge toDomain(ChargeEntity entity) {
        if (entity == null) {
            return null;
        }

        Charge charge = Charge.create(
            ChargeId.valueOf(entity.getChargeId()),
            AccountId.valueOf(entity.getAccountId()),
            ChargeType.fromCode(entity.getChargeType()),
            entity.getAmount()
        );

        // Restore state from entity
        if (entity.getAccountStatus() != null) {
            charge.startAccountValidation(new AccountStatus(entity.getAccountStatus()));
        }

        if (entity.getChargeStatus() != null) {
            try {
                ChargeStatus status = ChargeStatus.valueOf(entity.getChargeStatus());
                // Use reflection to set status (workaround for immutable pattern)
                java.lang.reflect.Field statusField = Charge.class.getDeclaredField("chargeStatus");
                statusField.setAccessible(true);
                statusField.set(charge, status);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                // Log if needed, but continue
            }
        }

        if (entity.getProcessingResult() != null) {
            try {
                java.lang.reflect.Field resultField = Charge.class.getDeclaredField("processingResult");
                resultField.setAccessible(true);
                if ("SUCCESS".equals(entity.getProcessingResult())) {
                    resultField.set(charge, ProcessingResult.success());
                } else if (entity.getRejectionReason() != null) {
                    resultField.set(charge, ProcessingResult.failure(entity.getRejectionReason()));
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                // Log if needed, but continue
            }
        }

        if (entity.getRejectionReason() != null) {
            try {
                java.lang.reflect.Field reasonField = Charge.class.getDeclaredField("rejectionReason");
                reasonField.setAccessible(true);
                reasonField.set(charge, entity.getRejectionReason());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                // Log if needed, but continue
            }
        }

        if (entity.getProcessingDate() != null) {
            try {
                java.lang.reflect.Field dateField = Charge.class.getDeclaredField("processingDate");
                dateField.setAccessible(true);
                dateField.set(charge, entity.getProcessingDate());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                // Log if needed, but continue
            }
        }

        return charge;
    }
}

