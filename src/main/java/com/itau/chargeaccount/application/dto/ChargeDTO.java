package com.itau.chargeaccount.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Charge DTO used in the Application layer.
 * Serves as a contract for REST API and communication between layers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChargeDTO {

    @JsonProperty("id_charge")
    private String idCharge;

    @JsonProperty("id_account")
    private String idAccount;

    @JsonProperty("type")
    private String type;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("charge_status")
    private String chargeStatus;

    @JsonProperty("account_status")
    private String accountStatus;

    @JsonProperty("processing_result")
    private String processingResult;

    @JsonProperty("rejection_reason")
    private String rejectionReason;

    @JsonProperty("charge_date")
    private LocalDateTime chargeDate;

    @JsonProperty("processing_date")
    private LocalDateTime processingDate;
}

