package com.itau.chargeaccount.domain.entity;

import com.itau.chargeaccount.domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for the Charge entity.
 * Validates the business logic of a charge.
 */
@DisplayName("Charge Entity Tests")
class ChargeTest {

    private ChargeId chargeId;
    private AccountId accountId;
    private ChargeType type;
    private BigDecimal amount;

    @BeforeEach
    void setUp() {
        chargeId = ChargeId.generate();
        accountId = AccountId.valueOf("123456");
        type = ChargeType.DEBIT;
        amount = new BigDecimal("100.00");
    }

    @Test
    @DisplayName("Should create a valid charge")
    void shouldCreateValidCharge() {
        // Act
        Charge charge = Charge.create(chargeId, accountId, type, amount);

        // Assert
        assertThat(charge).isNotNull();
        assertThat(charge.getChargeId()).isEqualTo(chargeId);
        assertThat(charge.getAccountId()).isEqualTo(accountId);
        assertThat(charge.getType()).isEqualTo(type);
        assertThat(charge.getAmount()).isEqualTo(amount);
        assertThat(charge.getChargeStatus()).isEqualTo(ChargeStatus.PENDING_VALIDATION);
        assertThat(charge.getChargeDate()).isNotNull();
    }

    @Test
    @DisplayName("Should throw exception when ChargeId is null")
    void shouldThrowExceptionWhenChargeIdNull() {
        // Act & Assert
        assertThatThrownBy(() -> Charge.create(null, accountId, type, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ChargeId cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when AccountId is null")
    void shouldThrowExceptionWhenAccountIdNull() {
        // Act & Assert
        assertThatThrownBy(() -> Charge.create(chargeId, null, type, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("AccountId cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when type is null")
    void shouldThrowExceptionWhenTypeNull() {
        // Act & Assert
        assertThatThrownBy(() -> Charge.create(chargeId, accountId, null, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ChargeType cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when amount is null")
    void shouldThrowExceptionWhenAmountNull() {
        // Act & Assert
        assertThatThrownBy(() -> Charge.create(chargeId, accountId, type, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount must be greater than zero");
    }

    @Test
    @DisplayName("Should throw exception when amount is zero")
    void shouldThrowExceptionWhenAmountZero() {
        // Act & Assert
        assertThatThrownBy(() -> Charge.create(chargeId, accountId, type, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount must be greater than zero");
    }

    @Test
    @DisplayName("Should throw exception when amount is negative")
    void shouldThrowExceptionWhenAmountNegative() {
        // Act & Assert
        assertThatThrownBy(() -> Charge.create(chargeId, accountId, type, new BigDecimal("-100.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount must be greater than zero");
    }

    @Test
    @DisplayName("Should process charge successfully when account is ACTIVE")
    void shouldProcessSuccessfullyWhenAccountActive() {
        // Arrange
        Charge charge = Charge.create(chargeId, accountId, type, amount);
        AccountStatus activeStatus = AccountStatus.ACTIVE;

        // Act
        charge.startAccountValidation(activeStatus);
        charge.process();

        // Assert
        assertThat(charge.wasProcessedSuccessfully()).isTrue();
        assertThat(charge.getChargeStatus()).isEqualTo(ChargeStatus.PROCESSED);
        assertThat(charge.getProcessingResult().isSuccess()).isTrue();
        assertThat(charge.getRejectionReason()).isNull();
        assertThat(charge.getProcessingDate()).isNotNull();
    }

    @Test
    @DisplayName("Should reject charge when account is CANCELLED")
    void shouldRejectWhenAccountCancelled() {
        // Arrange
        Charge charge = Charge.create(chargeId, accountId, type, amount);
        AccountStatus cancelledStatus = AccountStatus.CANCELLED;

        // Act
        charge.startAccountValidation(cancelledStatus);
        charge.process();

        // Assert
        assertThat(charge.wasRejected()).isTrue();
        assertThat(charge.getChargeStatus()).isEqualTo(ChargeStatus.REJECTED);
        assertThat(charge.getProcessingResult().isFailure()).isTrue();
        assertThat(charge.getRejectionReason()).containsIgnoringCase("CANCELLED");
        assertThat(charge.getProcessingDate()).isNotNull();
    }

    @Test
    @DisplayName("Should reject charge when account is in LEGAL_HOLD")
    void shouldRejectWhenAccountBlocked() {
        // Arrange
        Charge charge = Charge.create(chargeId, accountId, type, amount);
        AccountStatus blockedStatus = AccountStatus.LEGAL_HOLD;

        // Act
        charge.startAccountValidation(blockedStatus);
        charge.process();

        // Assert
        assertThat(charge.wasRejected()).isTrue();
        assertThat(charge.getChargeStatus()).isEqualTo(ChargeStatus.REJECTED);
        assertThat(charge.getProcessingResult().isFailure()).isTrue();
        assertThat(charge.getRejectionReason()).containsIgnoringCase("LEGAL_HOLD");
    }

    @Test
    @DisplayName("Should fail to process without account status defined")
    void shouldFailToProcessWithoutAccountStatus() {
        // Arrange
        Charge charge = Charge.create(chargeId, accountId, type, amount);

        // Act
        charge.process();

        // Assert
        assertThat(charge.wasRejected()).isTrue();
        assertThat(charge.getProcessingResult().isFailure()).isTrue();
        assertThat(charge.getRejectionReason()).contains("not defined");
    }

    @Test
    @DisplayName("Should recognize successfully processed charge")
    void shouldRecognizeProcessedSuccessfully() {
        // Arrange
        Charge charge = Charge.create(chargeId, accountId, type, amount);
        charge.startAccountValidation(AccountStatus.ACTIVE);

        // Act
        boolean result = charge.wasProcessedSuccessfully();

        // Assert
        assertThat(result).isFalse(); // Not yet processed

        // Act - Process
        charge.process();
        result = charge.wasProcessedSuccessfully();

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should recognize rejected charge")
    void shouldRecognizeRejected() {
        // Arrange
        Charge charge = Charge.create(chargeId, accountId, type, amount);
        charge.startAccountValidation(AccountStatus.CANCELLED);

        // Act
        boolean result = charge.wasRejected();

        // Assert
        assertThat(result).isFalse(); // Not yet rejected

        // Act - Process
        charge.process();
        result = charge.wasRejected();

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Two charges with same ID should be equal")
    void shouldBeEqualWithSameId() {
        // Arrange
        Charge charge1 = Charge.create(chargeId, accountId, type, amount);
        Charge charge2 = Charge.create(chargeId, accountId, type, amount);

        // Assert
        assertThat(charge1).isEqualTo(charge2);
    }

    @Test
    @DisplayName("Two charges with different IDs should not be equal")
    void shouldNotBeEqualWithDifferentIds() {
        // Arrange
        Charge charge1 = Charge.create(chargeId, accountId, type, amount);
        Charge charge2 = Charge.create(ChargeId.generate(), accountId, type, amount);

        // Assert
        assertThat(charge1).isNotEqualTo(charge2);
    }
}

