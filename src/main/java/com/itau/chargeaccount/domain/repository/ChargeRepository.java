package com.itau.chargeaccount.domain.repository;

import com.itau.chargeaccount.domain.entity.Charge;
import com.itau.chargeaccount.domain.valueobject.ChargeId;

import java.util.Optional;

/**
 * Port (Interface) for charge persistence repository.
 * Implements the Hexagonal Architecture port pattern.
 *
 * The concrete implementation (Adapter) will be in the Infrastructure layer.
 */
public interface ChargeRepository {

    /**
     * Saves a charge in the repository.
     */
    void save(Charge charge);

    /**
     * Updates an existing charge in the repository.
     */
    void update(Charge charge);

    /**
     * Finds a charge by its ID.
     */
    Optional<Charge> findById(ChargeId chargeId);

    /**
     * Finds a charge by the account ID.
     */
    Optional<Charge> findByAccountId(String accountId);
}

