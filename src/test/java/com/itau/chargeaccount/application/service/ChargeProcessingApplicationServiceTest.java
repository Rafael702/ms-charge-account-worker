package com.itau.chargeaccount.application.service;

import com.itau.chargeaccount.application.dto.ChargeDTO;
import com.itau.chargeaccount.application.port.EventPublisherPort;
import com.itau.chargeaccount.domain.entity.Charge;
import com.itau.chargeaccount.domain.event.AccountValidatedEvent;
import com.itau.chargeaccount.domain.repository.ChargeRepository;
import com.itau.chargeaccount.domain.service.ChargeProcessingDomainService;
import com.itau.chargeaccount.domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChargeProcessingApplicationServiceTest {

    @Mock
    private ChargeRepository chargeRepository;

    @Mock
    private EventPublisherPort eventPublisherPort;

    private ChargeProcessingApplicationService appService;

    @BeforeEach
    void setUp() {
        ChargeProcessingDomainService domainService = new ChargeProcessingDomainService();
        appService = new ChargeProcessingApplicationService(
                chargeRepository,
                eventPublisherPort,
                domainService
        );
    }

    @Test
    @DisplayName("Should process a valid charge successfully")
    void shouldProcessValidCharge() {
        // Arrange
        ChargeDTO dto = ChargeDTO.builder()
                .idCharge("charge-001")
                .idAccount("account-001")
                .type("D")
                .amount(new BigDecimal("100.00"))
                .build();

        doNothing().when(chargeRepository).save(any(Charge.class));

        // Act
        appService.processCharge(dto);

        // Assert
        verify(chargeRepository, times(1)).save(any(Charge.class));
        verify(eventPublisherPort, times(1)).publish(any());
    }

    @Test
    @DisplayName("Should throw exception when charge ID is empty")
    void shouldThrowExceptionWhenChargeIdEmpty() {
        // Arrange
        ChargeDTO dto = ChargeDTO.builder()
                .idCharge("")
                .idAccount("account-001")
                .type("D")
                .amount(new BigDecimal("100.00"))
                .build();

        // Act & Assert
        assertThatThrownBy(() -> appService.processCharge(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Charge ID");
    }

    @Test
    @DisplayName("Should throw exception when account ID is null")
    void shouldThrowExceptionWhenAccountIdNull() {
        // Arrange
        ChargeDTO dto = ChargeDTO.builder()
                .idCharge("charge-001")
                .idAccount(null)
                .type("D")
                .amount(new BigDecimal("100.00"))
                .build();

        // Act & Assert
        assertThatThrownBy(() -> appService.processCharge(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account ID");
    }

    @Test
    @DisplayName("Should throw exception when type is invalid")
    void shouldThrowExceptionWhenTypeInvalid() {
        // Arrange
        ChargeDTO dto = ChargeDTO.builder()
                .idCharge("charge-001")
                .idAccount("account-001")
                .type("X")
                .amount(new BigDecimal("100.00"))
                .build();

        // Act & Assert
        assertThatThrownBy(() -> appService.processCharge(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid charge type");
    }

    @Test
    @DisplayName("Should throw exception when amount is negative")
    void shouldThrowExceptionWhenAmountNegative() {
        // Arrange
        ChargeDTO dto = ChargeDTO.builder()
                .idCharge("charge-001")
                .idAccount("account-001")
                .type("D")
                .amount(new BigDecimal("-100.00"))
                .build();

        // Act & Assert
        assertThatThrownBy(() -> appService.processCharge(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Amount");
    }

    @Test
    @DisplayName("Should finalize processing with account status received")
    void shouldFinalizeWithAccountStatus() {
        // Arrange
        Charge charge = Charge.create(
                ChargeId.valueOf("charge-001"),
                AccountId.valueOf("account-001"),
                ChargeType.DEBIT,
                new BigDecimal("100.00")
        );

        AccountValidatedEvent event = new AccountValidatedEvent(
                charge.getChargeId(),
                "account-001",
                AccountStatus.ACTIVE
        );

        when(chargeRepository.findById(charge.getChargeId()))
                .thenReturn(Optional.of(charge));

        // Act
        appService.finalizeProcessingWithAccountStatus(event);

        // Assert
        verify(chargeRepository, times(1)).findById(charge.getChargeId());
        verify(chargeRepository, times(1)).update(any(Charge.class));
        verify(eventPublisherPort, times(1)).publishToAccountingSystem(any());
    }

    @Test
    @DisplayName("Should throw exception when charge not found")
    void shouldThrowExceptionWhenChargeNotFound() {
        // Arrange
        AccountValidatedEvent event = new AccountValidatedEvent(
                ChargeId.valueOf("charge-nonexistent"),
                "account-001",
                AccountStatus.ACTIVE
        );

        when(chargeRepository.findById(any()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> appService.finalizeProcessingWithAccountStatus(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }
}

