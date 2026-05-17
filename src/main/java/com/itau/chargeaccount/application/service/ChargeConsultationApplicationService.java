package com.itau.chargeaccount.application.service;

import com.itau.chargeaccount.application.dto.ChargeDTO;
import com.itau.chargeaccount.application.usecase.ConsultChargeUseCase;
import com.itau.chargeaccount.domain.entity.Charge;
import com.itau.chargeaccount.domain.repository.ChargeRepository;
import com.itau.chargeaccount.domain.valueobject.ChargeId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Application Service for charge inquiry operations.
 * Responsibilities:
 * - Find processed charges
 * - Convert Entities to DTOs
 * - Apply filters and validations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChargeConsultationApplicationService implements ConsultChargeUseCase {

    private final ChargeRepository chargeRepository;

    /**
     * Consults a charge by its ID.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<ChargeDTO> findByChargeId(String chargeId) {
        log.debug("Consulting charge with ID: {}", chargeId);
        try {
            ChargeId id = ChargeId.valueOf(chargeId);
            return chargeRepository.findById(id).map(this::convertToDTO);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid charge ID: {}", chargeId);
            throw e;
        } catch (Exception e) {
            log.error("Error consulting charge {}: {}", chargeId, e.getMessage(), e);
            throw new RuntimeException("Error consulting charge", e);
        }
    }

    /**
     * Consults a charge by account ID.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<ChargeDTO> findByAccountId(String accountId) {
        log.debug("Consulting charge for account: {}", accountId);
        try {
            return chargeRepository.findByAccountId(accountId).map(this::convertToDTO);
        } catch (Exception e) {
            log.error("Error consulting charges for account {}: {}", accountId, e.getMessage(), e);
            throw new RuntimeException("Error consulting charges", e);
        }
    }

    // Keep backward-compatible aliases
    @Transactional(readOnly = true)
    public Optional<ChargeDTO> consultCharge(String chargeId) {
        return findByChargeId(chargeId);
    }

    @Transactional(readOnly = true)
    public Optional<ChargeDTO> consultChargeByAccount(String accountId) {
        return findByAccountId(accountId);
    }

    /**
     * Converts a Charge Entity to ChargeDTO.
     */
    private ChargeDTO convertToDTO(Charge charge) {
        return ChargeDTO.builder()
                .idCharge(charge.getChargeId().getValue())
                .idAccount(charge.getAccountId().getValue())
                .type(charge.getType().getCode())
                .amount(charge.getAmount())
                .chargeStatus(charge.getChargeStatus().name())
                .accountStatus(charge.getAccountStatus() != null ? charge.getAccountStatus().value() : null)
                .processingResult(charge.getProcessingResult() != null
                        ? charge.getProcessingResult().getStatus()
                        : null)
                .rejectionReason(charge.getRejectionReason())
                .chargeDate(charge.getChargeDate())
                .processingDate(charge.getProcessingDate())
                .build();
    }
}
