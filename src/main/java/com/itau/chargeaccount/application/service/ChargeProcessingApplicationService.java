package com.itau.chargeaccount.application.service;

import com.itau.chargeaccount.application.dto.ChargeDTO;
import com.itau.chargeaccount.application.dto.ProcessingResultDTO;
import com.itau.chargeaccount.application.port.EventPublisherPort;
import com.itau.chargeaccount.domain.entity.Charge;
import com.itau.chargeaccount.domain.event.AccountValidatedEvent;
import com.itau.chargeaccount.domain.event.ChargeProcessedEvent;
import com.itau.chargeaccount.domain.event.ChargeReceivedEvent;
import com.itau.chargeaccount.domain.repository.ChargeRepository;
import com.itau.chargeaccount.domain.service.ChargeProcessingDomainService;
import com.itau.chargeaccount.domain.valueobject.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Application Service that orchestrates charge processing.
 *
 * Responsibilities:
 * - Convert DTOs to Domain Entities
 * - Coordinate processing flow
 * - Publish events
 * - Persist results
 *
 * Implicitly follows the CQRS pattern: receives commands and fires events.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChargeProcessingApplicationService {

    private final ChargeRepository chargeRepository;
    private final EventPublisherPort eventPublisherPort;
    private final ChargeProcessingDomainService chargeProcessingDomainService;

    /**
     * Processes a charge completely.
     *
     * Flow:
     * 1. Creates the Domain Entity from the DTO
     * 2. Publishes event requesting account validation
     * 3. Persists the charge in PENDING_VALIDATION state
     */
    @Transactional
    public void processCharge(ChargeDTO chargeDTO) {
        log.info("Starting charge processing: {}", chargeDTO.getIdCharge());

        try {
            // Validate DTO
            validateChargeDTO(chargeDTO);

            // Convert DTO to Domain Entity
            Charge charge = convertDTOToEntity(chargeDTO);

            // Persist in repository (initial state: PENDING_VALIDATION)
            chargeRepository.save(charge);

            // Publish event requesting account validation
            ChargeReceivedEvent event = new ChargeReceivedEvent(
                    charge.getChargeId(),
                    charge.getAccountId().getValue(),
                    charge.getType().getCode(),
                    charge.getAmount()
            );
            eventPublisherPort.publish(event);
            log.info("Charge received event published: {}", event.getEventId());

        } catch (IllegalArgumentException e) {
            log.warn("Error validating charge DTO: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error processing charge {}: {}", chargeDTO.getIdCharge(), e.getMessage(), e);
            throw new RuntimeException("Error processing charge", e);
        }
    }

    /**
     * Processes a charge when it receives the account validation event.
     *
     * Flow:
     * 1. Find the charge in the repository
     * 2. Register the account status
     * 3. Execute processing logic
     * 4. Publish result event
     * 5. Persist the updated charge
     */
    @Transactional
    public void finalizeProcessingWithAccountStatus(AccountValidatedEvent event) {
        log.info("Finalizing processing with account status: {}", event.getEventId());

        try {
            // Find existing charge
            Charge charge = chargeRepository.findById(event.getChargeId())
                    .orElseThrow(() -> new RuntimeException("Charge not found: " + event.getChargeId()));

            // Execute Domain Service to process with account status
            chargeProcessingDomainService.processChargeWithAccountStatus(
                    charge,
                    event.getAccountStatus()
            );

            // Update in repository
            chargeRepository.update(charge);
            log.debug("Charge updated with status: {}", charge.getChargeStatus());

            // Publish result event
            ChargeProcessedEvent resultEvent = new ChargeProcessedEvent(
                    charge.getChargeId(),
                    charge.getAccountId().getValue(),
                    charge.getType().getCode(),
                    charge.getAmount(),
                    charge.getProcessingResult()
            );

            if (charge.wasProcessedSuccessfully()) {
                eventPublisherPort.publishToAccountingSystem(resultEvent);
                log.info("Charge processed successfully. Event sent to accounting system.");
            } else {
                eventPublisherPort.publish(resultEvent);
                log.warn("Charge rejected: {}", charge.getRejectionReason());
            }

        } catch (Exception e) {
            log.error("Error finalizing charge processing: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Alias method for KafkaListenerAdapter compatibility.
     * Delegates to finalizeProcessingWithAccountStatus.
     */
    @Transactional
    public void finalizeProcessing(AccountValidatedEvent event) {
        finalizeProcessingWithAccountStatus(event);
    }

    /**
     * Converts a ChargeDTO to the Domain Entity Charge.
     */
    private Charge convertDTOToEntity(ChargeDTO dto) {
        ChargeId chargeId = ChargeId.valueOf(dto.getIdCharge());
        AccountId accountId = AccountId.valueOf(dto.getIdAccount());
        ChargeType type = ChargeType.fromCode(dto.getType());
        BigDecimal amount = dto.getAmount();

        return Charge.create(chargeId, accountId, type, amount);
    }

    /**
     * Validates a ChargeDTO.
     */
    private void validateChargeDTO(ChargeDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("ChargeDTO cannot be null");
        }
        if (dto.getIdCharge() == null || dto.getIdCharge().trim().isEmpty()) {
            throw new IllegalArgumentException("Charge ID is required");
        }
        if (dto.getIdAccount() == null || dto.getIdAccount().trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID is required");
        }
        if (dto.getType() == null || dto.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("Charge type is required");
        }
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }

    /**
     * Converts an Entity to ProcessingResultDTO.
     */
    public ProcessingResultDTO convertToResultDTO(Charge charge) {
        return ProcessingResultDTO.builder()
                .idCharge(charge.getChargeId().getValue())
                .status(charge.getProcessingResult() != null
                    ? charge.getProcessingResult().getStatus()
                    : "PENDING")
                .rejectionReason(charge.getRejectionReason())
                .processedAt(charge.getProcessingDate() != null
                    ? charge.getProcessingDate().toString()
                    : null)
                .build();
    }
}
