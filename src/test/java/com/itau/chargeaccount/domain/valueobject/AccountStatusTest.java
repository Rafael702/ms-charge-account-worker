package com.itau.chargeaccount.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for the AccountStatus Value Object.
 */
@DisplayName("AccountStatus Value Object Tests")
class AccountStatusTest {

    @Test
    @DisplayName("Should create ACTIVE status")
    void shouldCreateActiveStatus() {
        // Assert
        assertThat(AccountStatus.ACTIVE).isNotNull();
        assertThat(AccountStatus.ACTIVE.value()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("Should create CANCELLED status")
    void shouldCreateCancelledStatus() {
        // Assert
        assertThat(AccountStatus.CANCELLED).isNotNull();
        assertThat(AccountStatus.CANCELLED.value()).isEqualTo("CANCELLED");
    }

    @Test
    @DisplayName("Should create LEGAL_HOLD status")
    void shouldCreateLegalHoldStatus() {
        // Assert
        assertThat(AccountStatus.LEGAL_HOLD).isNotNull();
        assertThat(AccountStatus.LEGAL_HOLD.value()).isEqualTo("LEGAL_HOLD");
    }

    @Test
    @DisplayName("ACTIVE account can process charges")
    void shouldAllowProcessingWhenActive() {
        // Assert
        assertThat(AccountStatus.ACTIVE.canProcess()).isTrue();
    }

    @Test
    @DisplayName("CANCELLED account cannot process charges")
    void shouldNotAllowProcessingWhenCancelled() {
        // Assert
        assertThat(AccountStatus.CANCELLED.canProcess()).isFalse();
    }

    @Test
    @DisplayName("Account in LEGAL_HOLD cannot process charges")
    void shouldNotAllowProcessingWhenBlocked() {
        // Assert
        assertThat(AccountStatus.LEGAL_HOLD.canProcess()).isFalse();
    }

    @Test
    @DisplayName("ACTIVE account is not blocked")
    void shouldIndicateNotBlockedWhenActive() {
        // Assert
        assertThat(AccountStatus.ACTIVE.isBlocked()).isFalse();
    }

    @Test
    @DisplayName("CANCELLED account is blocked")
    void shouldIndicateBlockedWhenCancelled() {
        // Assert
        assertThat(AccountStatus.CANCELLED.isBlocked()).isTrue();
    }

    @Test
    @DisplayName("Account in LEGAL_HOLD is blocked")
    void shouldIndicateBlockedWhenLegalHold() {
        // Assert
        assertThat(AccountStatus.LEGAL_HOLD.isBlocked()).isTrue();
    }

    @Test
    @DisplayName("Two equal AccountStatus should be equal")
    void shouldBeEqual() {
        // Arrange
        AccountStatus status1 = AccountStatus.ACTIVE;
        AccountStatus status2 = AccountStatus.ACTIVE;

        // Assert
        assertThat(status1).isEqualTo(status2);
    }

    @Test
    @DisplayName("Two different AccountStatus should not be equal")
    void shouldNotBeEqual() {
        // Assert
        assertThat(AccountStatus.ACTIVE).isNotEqualTo(AccountStatus.CANCELLED);
    }

    @Test
    @DisplayName("AccountStatus should convert to string correctly")
    void shouldConvertToString() {
        // Assert
        assertThat(AccountStatus.ACTIVE.toString()).isEqualTo("ACTIVE");
        assertThat(AccountStatus.CANCELLED.toString()).isEqualTo("CANCELLED");
        assertThat(AccountStatus.LEGAL_HOLD.toString()).isEqualTo("LEGAL_HOLD");
    }
}

