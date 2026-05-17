package com.itau.chargeaccount.domain.service;

import com.itau.chargeaccount.domain.entity.Charge;
import com.itau.chargeaccount.domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for the ChargeProcessingDomainService.
 */
@DisplayName("ChargeProcessingDomainService Tests")
class ChargeProcessingDomainServiceTest {

    private ChargeProcessingDomainService domainService;
    private Charge charge;

    @BeforeEach
    void setUp() {
        domainService = new ChargeProcessingDomainService();
        charge = Charge.create(
                ChargeId.generate(),
                AccountId.valueOf("123456"),
                ChargeType.DEBIT,
                new BigDecimal("100.00")
        );
    }

    @Test
    @DisplayName("Should process charge successfully with ACTIVE account")
    void shouldProcessSuccessfullyWithActiveAccount() {
        // Act
        domainService.processChargeWithAccountStatus(charge, AccountStatus.ACTIVE);

        // Assert
        assertThat(charge.wasProcessedSuccessfully()).isTrue();
        assertThat(charge.getChargeStatus()).isEqualTo(ChargeStatus.PROCESSED);
        assertThat(charge.getProcessingResult().isSuccess()).isTrue();
    }

    @Test
    @DisplayName("Should reject charge with CANCELLED account")
    void shouldRejectWithCancelledAccount() {
        // Act
        domainService.processChargeWithAccountStatus(charge, AccountStatus.CANCELLED);

        // Assert
        assertThat(charge.wasRejected()).isTrue();
        assertThat(charge.getChargeStatus()).isEqualTo(ChargeStatus.REJECTED);
        assertThat(charge.getProcessingResult().isFailure()).isTrue();
    }

    @Test
    @DisplayName("Should reject charge with LEGAL_HOLD account")
    void shouldRejectWithBlockedAccount() {
        // Act
        domainService.processChargeWithAccountStatus(charge, AccountStatus.LEGAL_HOLD);

        // Assert
        assertThat(charge.wasRejected()).isTrue();
        assertThat(charge.getChargeStatus()).isEqualTo(ChargeStatus.REJECTED);
        assertThat(charge.getProcessingResult().isFailure()).isTrue();
    }

    @Test
    @DisplayName("Should throw exception when charge is null")
    void shouldThrowExceptionWhenChargeNull() {
        // Act & Assert
        assertThatThrownBy(() -> domainService.processChargeWithAccountStatus(null, AccountStatus.ACTIVE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Charge cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when account status is null")
    void shouldThrowExceptionWhenAccountStatusNull() {
        // Act & Assert
        assertThatThrownBy(() -> domainService.processChargeWithAccountStatus(charge, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("AccountStatus cannot be null");
    }

    @Test
    @DisplayName("Should update charge status to VALIDATING_ACCOUNT")
    void shouldUpdateStatusToValidating() {
        // Act
        domainService.processChargeWithAccountStatus(charge, AccountStatus.ACTIVE);

        // Assert
        assertThat(charge.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(charge.getChargeStatus()).isNotEqualTo(ChargeStatus.PENDING_VALIDATION);
    }
}

