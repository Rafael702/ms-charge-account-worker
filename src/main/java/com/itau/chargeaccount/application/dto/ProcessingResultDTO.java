package com.itau.chargeaccount.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO that represents the result of processing a charge.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessingResultDTO {

    @JsonProperty("id_charge")
    private String idCharge;

    @JsonProperty("status")
    private String status;

    @JsonProperty("rejection_reason")
    private String rejectionReason;

    @JsonProperty("processed_at")
    private String processedAt;
}

