package com.itau.chargeaccount.infrastructure.file;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for mapping CSV records to Java objects.
 * Used by Jackson's CSV mapper for deserializing CSV lines.
 *
 * Expected CSV format:
 * chargeId,accountId,chargeType,amount
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChargeCSVRecord {

    @JsonProperty("chargeId")
    private String chargeId;

    @JsonProperty("accountId")
    private String accountId;

    @JsonProperty("chargeType")
    private String chargeType;

    @JsonProperty("amount")
    private String amount;
}

