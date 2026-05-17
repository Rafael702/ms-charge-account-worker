package com.itau.chargeaccount.presentation.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.itau.chargeaccount.application.dto.ChargeDTO;
import com.itau.chargeaccount.application.usecase.ConsultChargeUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ChargeConsultationController.
 * Uses MockMvc to validate HTTP layer behavior without starting the full application.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChargeConsultationController Unit Tests")
class ChargeConsultationControllerTest {

    @Mock
    private ConsultChargeUseCase consultChargeUseCase;

    @InjectMocks
    private ChargeConsultationController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("Should return 200 with charge data when charge is found by ID")
    void shouldReturn200WhenChargeFoundById() throws Exception {
        // Given
        ChargeDTO charge = buildChargeDTO("CHARGE-001", "ACCOUNT-123");
        when(consultChargeUseCase.findByChargeId("CHARGE-001")).thenReturn(Optional.of(charge));

        // When / Then
        mockMvc.perform(get("/charges/CHARGE-001")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_charge").value("CHARGE-001"))
                .andExpect(jsonPath("$.id_account").value("ACCOUNT-123"))
                .andExpect(jsonPath("$.type").value("D"))
                .andExpect(jsonPath("$.charge_status").value("PROCESSED"));

        verify(consultChargeUseCase).findByChargeId("CHARGE-001");
    }

    @Test
    @DisplayName("Should return 404 when charge is not found by ID")
    void shouldReturn404WhenChargeNotFoundById() throws Exception {
        // Given
        when(consultChargeUseCase.findByChargeId("CHARGE-999")).thenReturn(Optional.empty());

        // When / Then
        mockMvc.perform(get("/charges/CHARGE-999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(consultChargeUseCase).findByChargeId("CHARGE-999");
    }

    @Test
    @DisplayName("Should return 200 with charge data when charge is found by account ID")
    void shouldReturn200WhenChargeFoundByAccountId() throws Exception {
        // Given
        ChargeDTO charge = buildChargeDTO("CHARGE-001", "ACCOUNT-123");
        when(consultChargeUseCase.findByAccountId("ACCOUNT-123")).thenReturn(Optional.of(charge));

        // When / Then
        mockMvc.perform(get("/charges/account/ACCOUNT-123")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_charge").value("CHARGE-001"))
                .andExpect(jsonPath("$.id_account").value("ACCOUNT-123"));

        verify(consultChargeUseCase).findByAccountId("ACCOUNT-123");
    }

    @Test
    @DisplayName("Should return 404 when no charge found for account ID")
    void shouldReturn404WhenChargeNotFoundByAccountId() throws Exception {
        // Given
        when(consultChargeUseCase.findByAccountId("ACCOUNT-999")).thenReturn(Optional.empty());

        // When / Then
        mockMvc.perform(get("/charges/account/ACCOUNT-999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(consultChargeUseCase).findByAccountId("ACCOUNT-999");
    }

    @Test
    @DisplayName("Should return charge with REJECTED status and rejection reason")
    void shouldReturnRejectedChargeWithRejectionReason() throws Exception {
        // Given
        ChargeDTO charge = ChargeDTO.builder()
                .idCharge("CHARGE-002")
                .idAccount("ACCOUNT-456")
                .type("D")
                .amount(BigDecimal.valueOf(500.00))
                .chargeStatus("REJECTED")
                .accountStatus("CANCELADA")
                .processingResult("FAILURE")
                .rejectionReason("Account with status CANCELADA cannot process charges")
                .chargeDate(LocalDateTime.now())
                .build();
        when(consultChargeUseCase.findByChargeId("CHARGE-002")).thenReturn(Optional.of(charge));

        // When / Then
        mockMvc.perform(get("/charges/CHARGE-002")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.charge_status").value("REJECTED"))
                .andExpect(jsonPath("$.rejection_reason").isNotEmpty());

        verify(consultChargeUseCase).findByChargeId("CHARGE-002");
    }

    private ChargeDTO buildChargeDTO(String chargeId, String accountId) {
        return ChargeDTO.builder()
                .idCharge(chargeId)
                .idAccount(accountId)
                .type("D")
                .amount(BigDecimal.valueOf(100.00))
                .chargeStatus("PROCESSED")
                .accountStatus("ATIVA")
                .processingResult("SUCCESS")
                .chargeDate(LocalDateTime.now())
                .processingDate(LocalDateTime.now())
                .build();
    }
}

