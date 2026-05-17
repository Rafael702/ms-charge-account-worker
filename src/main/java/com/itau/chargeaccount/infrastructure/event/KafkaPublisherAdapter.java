package com.itau.chargeaccount.infrastructure.event;

import com.itau.chargeaccount.application.port.EventPublisherPort;
import com.itau.chargeaccount.domain.event.DomainEvent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * Kafka implementation of EventPublisherPort.
 * Publishes domain events to Kafka topics for asynchronous processing.
 *
 * Implements Resilience4j patterns:
 * - @Retry: Automatic retries with exponential backoff
 * - @CircuitBreaker: Prevents cascade failures
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaPublisherAdapter implements EventPublisherPort {

    private final KafkaTemplate<String, DomainEvent> kafkaTemplate;

    @Value("${app.kafka.topics.charge-events}")
    private String chargeEventsTopic;

    @Value("${app.kafka.topics.accounting-events}")
    private String accountingEventsTopic;

    /**
     * Publishes an event to the charge events topic.
     * Used for charge.received events to request account validation.
     *
     * @param event The domain event to publish
     * @throws RuntimeException if publication fails after retries
     */
    @Override
    @Retry(name = "kafkaPublisher")
    @CircuitBreaker(name = "kafkaPublisher", fallbackMethod = "publishFallback")
    public void publish(DomainEvent event) {
        try {
            log.info("Publishing event to topic '{}': {}", chargeEventsTopic, event.getEventName());

            Message<DomainEvent> message = MessageBuilder
                    .withPayload(event)
                    .setHeader(KafkaHeaders.TOPIC, chargeEventsTopic)
                    .setHeader("eventType", event.getEventName())
                    .build();

            kafkaTemplate.send(message);
            log.debug("Event published successfully: {}", event.getEventName());
        } catch (Exception e) {
            log.error("Error publishing event: {}", event.getEventName(), e);
            throw new RuntimeException("Failed to publish event: " + event.getEventName(), e);
        }
    }

    /**
     * Publishes an event to the accounting system topic.
     * Used for charge.processed events when a charge is successfully processed.
     *
     * @param event The domain event to publish to accounting system
     * @throws RuntimeException if publication fails after retries
     */
    @Override
    @Retry(name = "kafkaPublisher")
    @CircuitBreaker(name = "kafkaPublisher", fallbackMethod = "publishToAccountingSystemFallback")
    public void publishToAccountingSystem(DomainEvent event) {
        try {
            log.info("Publishing event to accounting topic '{}': {}", accountingEventsTopic, event.getEventName());

            Message<DomainEvent> message = MessageBuilder
                    .withPayload(event)
                    .setHeader(KafkaHeaders.TOPIC, accountingEventsTopic)
                    .setHeader("eventType", event.getEventName())
                    .build();

            kafkaTemplate.send(message);
            log.debug("Event published to accounting system successfully: {}", event.getEventName());
        } catch (Exception e) {
            log.error("Error publishing event to accounting system: {}", event.getEventName(), e);
            throw new RuntimeException("Failed to publish event to accounting system: " + event.getEventName(), e);
        }
    }

    /**
     * Fallback method when circuit breaker is open or max retries exceeded.
     * Logs the failure and throws an exception.
     */
    public void publishFallback(DomainEvent event, Exception ex) {
        log.error("Circuit breaker opened or max retries exceeded for event: {}. Cause: {}",
                event.getEventName(), ex.getMessage());
        throw new RuntimeException("Unable to publish event due to system failure: " + event.getEventName(), ex);
    }

    /**
     * Fallback method for publishing to accounting system.
     */
    public void publishToAccountingSystemFallback(DomainEvent event, Exception ex) {
        log.error("Circuit breaker opened or max retries exceeded for accounting event: {}. Cause: {}",
                event.getEventName(), ex.getMessage());
        throw new RuntimeException("Unable to publish event to accounting system due to system failure: " + event.getEventName(), ex);
    }
}

