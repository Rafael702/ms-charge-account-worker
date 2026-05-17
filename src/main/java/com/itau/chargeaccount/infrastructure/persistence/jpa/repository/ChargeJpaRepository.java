package com.itau.chargeaccount.infrastructure.persistence.jpa.repository;

import com.itau.chargeaccount.infrastructure.persistence.jpa.entity.ChargeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository for ChargeEntity.
 * Provides database access for charge persistence operations.
 */
@Repository
public interface ChargeJpaRepository extends JpaRepository<ChargeEntity, Long> {

    Optional<ChargeEntity> findByChargeId(String chargeId);

    Optional<ChargeEntity> findByAccountId(String accountId);
}

