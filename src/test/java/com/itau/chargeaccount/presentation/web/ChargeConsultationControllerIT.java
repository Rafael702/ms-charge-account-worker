package com.itau.chargeaccount.presentation.web;

import com.itau.chargeaccount.application.dto.ChargeDTO;
import com.itau.chargeaccount.application.usecase.ConsultChargeUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ChargeConsultationController using @WebMvcTest.
 * Loads only the web layer (Spring MVC) without starting the full context.
 */
@WebMvcTest(ChargeConsultationController.class)
@DisplayName("ChargeConsultationController Integration Tests")
class ChargeConsultationControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConsultChargeUseCase consultChargeUseCase;

    @Test
    @DisplayName("Should return 200 and charge JSON when charge exists by ID")
    void shouldReturn200AndChargeJsonWhenChargeExistsById() throws Exception {
        // Given
        ChargeDTO charge = buildProcessedCharge("CHARGE-001", "ACCOUNT-100");
        when(consultChargeUseCase.findByChargeId("CHARGE-001")).thenReturn(Optional.of(charge));

        // When / Then
        mockMvc.perform(get("/charges/CHARGE-001")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id_charge").value("CHARGE-001"))
                .andExpect(jsonPath("$.id_account").value("ACCOUNT-100"))
                .andExpect(jsonPath("$.type").value("D"))
                .andExpect(jsonPath("$.amount").value(150.00))
                .andExpect(jsonPath("$.charge_status").value("PROCESSED"))
                .andExpect(jsonPath("$.account_status").value("ATIVA"))
                .andExpect(jsonPath("$.processing_result").value("SUCCESS"));

        verify(consultChargeUseCase, times(1)).findByChargeId("CHARGE-001");
    }

    @Test
    @DisplayName("Should return 404 when charge does not exist by ID")
    void shouldReturn404WhenChargeDoesNotExistById() throws Exception {
        // Given
        when(consultChargeUseCase.findByChargeId("CHARGE-NOT-FOUND")).thenReturn(Optional.empty());

        // When / Then
        mockMvc.perform(get("/charges/CHARGE-NOT-FOUND")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(consultChargeUseCase, times(1)).findByChargeId("CHARGE-NOT-FOUND");
    }

    @Test
    @DisplayName("Should return 200 and charge JSON when charge exists by account ID")
    void shouldReturn200AndChargeJsonWhenChargeExistsByAccountId() throws Exception {
        // Given
        ChargeDTO charge = buildProcessedCharge("CHARGE-002", "ACCOUNT-200");
        when(consultChargeUseCase.findByAccountId("ACCOUNT-200")).thenReturn(Optional.of(charge));

        // When / Then
        mockMvc.perform(get("/charges/account/ACCOUNT-200")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_charge").value("CHARGE-002"))
                .andExpect(jsonPath("$.id_account").value("ACCOUNT-200"));

        verify(consultChargeUseCase, times(1)).findByAccountId("ACCOUNT-200");
    }

    @Test
    @DisplayName("Should return 404 when no charge found for account ID")
    void shouldReturn404WhenNoChargeFoundForAccountId() throws Exception {
        // Given
        when(consultChargeUseCase.findByAccountId("ACCOUNT-NOT-FOUND")).thenReturn(Optional.empty());

        // When / Then
        mockMvc.perform(get("/charges/account/ACCOUNT-NOT-FOUND")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(consultChargeUseCase, times(1)).findByAccountId("ACCOUNT-NOT-FOUND");
    }

    @Test
    @DisplayName("Should return rejected charge with rejection reason")
    void shouldReturnRejectedChargeWithRejectionReason() throws Exception {
        // Given
        ChargeDTO rejectedCharge = ChargeDTO.builder()
                .idCharge("CHARGE-003")
                .idAccount("ACCOUNT-300")
                .type("D")
                .amount(BigDecimal.valueOf(999.99))
                .chargeStatus("REJECTED")
                .accountStatus("BLOQUEIO_JUDICIAL")
                .processingResult("FAILURE")
                .rejectionReason("Account with status BLOQUEIO_JUDICIAL cannot process charges")
                .chargeDate(LocalDateTime.now())
                .build();
        when(consultChargeUseCase.findByChargeId("CHARGE-003")).thenReturn(Optional.of(rejectedCharge));

        // When / Then
        mockMvc.perform(get("/charges/CHARGE-003")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.charge_status").value("REJECTED"))
                .andExpect(jsonPath("$.account_status").value("BLOQUEIO_JUDICIAL"))
                .andExpect(jsonPath("$.processing_result").value("FAILURE"))
                .andExpect(jsonPath("$.rejection_reason").value(
                        "Account with status BLOQUEIO_JUDICIAL cannot process charges"));

        verify(consultChargeUseCase, times(1)).findByChargeId("CHARGE-003");
    }

    private ChargeDTO buildProcessedCharge(String chargeId, String accountId) {
        return ChargeDTO.builder()
                .idCharge(chargeId)
                .idAccount(accountId)
                .type("D")
                .amount(BigDecimal.valueOf(150.00))
                .chargeStatus("PROCESSED")
                .accountStatus("ATIVA")
                .processingResult("SUCCESS")
                .chargeDate(LocalDateTime.now())
                .processingDate(LocalDateTime.now())
                .build();
    }
}

