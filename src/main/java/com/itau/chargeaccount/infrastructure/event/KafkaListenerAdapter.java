package com.itau.chargeaccount.infrastructure.event;

import com.itau.chargeaccount.application.service.ChargeProcessingApplicationService;
import com.itau.chargeaccount.domain.event.AccountValidatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka listener adapter for consuming account validation events.
 *
 * Responsibilities:
 * - Listen to account.status.response topic for AccountValidatedEvent
 * - Call ChargeProcessingApplicationService to finalize charge processing
 * - Handle errors and send to Dead Letter Queue
 *
 * Partition strategy: One listener per partition for better throughput
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaListenerAdapter {

    private final ChargeProcessingApplicationService chargeProcessingApplicationService;

    /**
     * Listens to account.status.response topic for account validation events.
     *
     * When the account system responds with the account status, this listener:
     * 1. Receives the AccountValidatedEvent
     * 2. Calls the application service to finalize processing
     * 3. Persists the result to the database
     *
     * @param event The AccountValidatedEvent received from Kafka
     */
    @KafkaListener(
            topics = "${app.kafka.topics.account-status}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenAccountValidatedEvent(@Payload AccountValidatedEvent event) {
        try {
            log.info("Received AccountValidatedEvent for charge: {}, account: {}, status: {}",
                    event.getChargeId(), event.getAccountId(), event.getAccountStatus());

            // Finalize charge processing with the account status
            chargeProcessingApplicationService.finalizeProcessing(event);

            log.debug("Charge processing finalized successfully: {}", event.getChargeId());
        } catch (Exception e) {
            log.error("Error processing AccountValidatedEvent for charge: {}. Error: {}",
                    event.getChargeId(), e.getMessage(), e);
            // In a real scenario, this would be sent to a Dead Letter Queue
            throw new RuntimeException("Failed to process AccountValidatedEvent", e);
        }
    }
}

