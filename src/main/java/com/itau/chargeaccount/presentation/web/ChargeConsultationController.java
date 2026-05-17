package com.itau.chargeaccount.presentation.web;

import com.itau.chargeaccount.application.dto.ChargeDTO;
import com.itau.chargeaccount.application.usecase.ConsultChargeUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for online consultation of processed charges.
 *
 * Driving adapter (Primary Adapter) in the Hexagonal Architecture.
 * Exposes endpoints to query charge processing results.
 *
 * Endpoints:
 * - GET /charges/{chargeId}            → query by charge ID
 * - GET /charges/account/{accountId}   → query by account ID
 */
@Slf4j
@RestController
@RequestMapping("/charges")
@RequiredArgsConstructor
public class ChargeConsultationController {

    private final ConsultChargeUseCase consultChargeUseCase;

    /**
     * GET /charges/{chargeId}
     *
     * Retrieves the processing result of a specific charge.
     *
     * @param chargeId the charge identifier
     * @return 200 with charge data, or 404 if not found
     */
    @GetMapping("/{chargeId}")
    public ResponseEntity<ChargeDTO> findByChargeId(@PathVariable String chargeId) {
        log.info("Consulting charge by ID: {}", chargeId);

        return consultChargeUseCase.findByChargeId(chargeId)
                .map(charge -> {
                    log.debug("Charge found: {}", chargeId);
                    return ResponseEntity.ok(charge);
                })
                .orElseGet(() -> {
                    log.warn("Charge not found: {}", chargeId);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * GET /charges/account/{accountId}
     *
     * Retrieves the processing result for a charge associated with an account.
     *
     * @param accountId the account identifier
     * @return 200 with charge data, or 404 if not found
     */
    @GetMapping("/account/{accountId}")
    public ResponseEntity<ChargeDTO> findByAccountId(@PathVariable String accountId) {
        log.info("Consulting charge by account ID: {}", accountId);

        return consultChargeUseCase.findByAccountId(accountId)
                .map(charge -> {
                    log.debug("Charge found for account: {}", accountId);
                    return ResponseEntity.ok(charge);
                })
                .orElseGet(() -> {
                    log.warn("Charge not found for account: {}", accountId);
                    return ResponseEntity.notFound().build();
                });
    }
}

