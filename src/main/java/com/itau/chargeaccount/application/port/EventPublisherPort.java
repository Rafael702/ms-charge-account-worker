package com.itau.chargeaccount.application.port;

import com.itau.chargeaccount.domain.event.DomainEvent;

/**
 * Port (Interface) for publishing domain events.
 * Implements the Hexagonal Architecture port pattern.
 *
 * The concrete implementation (Adapter) will be in the Infrastructure layer (ex: KafkaPublisherAdapter).
 */
public interface EventPublisherPort {

    /**
     * Publishes a domain event to the system.
     * The event will be consumed by interested listeners.
     */
    void publish(DomainEvent event);

    /**
     * Publishes an event to the accounting system.
     * Used when a charge is processed successfully.
     */
    void publishToAccountingSystem(DomainEvent event);
}

