package com.itau.chargeaccount.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO to represent the status of an account.
 * Used in communication with the account system via events.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountStatusDTO {

    @JsonProperty("id_account")
    private String idAccount;

    @JsonProperty("status")
    private String status;  // ACTIVE, CANCELLED, LEGAL_HOLD

    @JsonProperty("message")
    private String message;
}

