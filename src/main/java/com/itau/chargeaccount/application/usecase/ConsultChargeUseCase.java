package com.itau.chargeaccount.application.usecase;

import com.itau.chargeaccount.application.dto.ChargeDTO;

import java.util.Optional;

/**
 * Use case for consulting processed charges.
 *
 * Allows online querying of charge processing results
 * by charge ID or by account ID.
 */
public interface ConsultChargeUseCase {

    /**
     * Finds a processed charge by its ID.
     *
     * @param chargeId the charge identifier
     * @return an Optional containing the charge DTO, or empty if not found
     */
    Optional<ChargeDTO> findByChargeId(String chargeId);

    /**
     * Finds a processed charge by account ID.
     *
     * @param accountId the account identifier
     * @return an Optional containing the charge DTO, or empty if not found
     */
    Optional<ChargeDTO> findByAccountId(String accountId);
}

