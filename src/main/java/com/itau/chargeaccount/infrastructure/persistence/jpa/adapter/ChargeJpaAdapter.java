package com.itau.chargeaccount.infrastructure.persistence.jpa.adapter;

import com.itau.chargeaccount.domain.entity.Charge;
import com.itau.chargeaccount.domain.repository.ChargeRepository;
import com.itau.chargeaccount.domain.valueobject.ChargeId;
import com.itau.chargeaccount.infrastructure.persistence.jpa.mapper.ChargeMapper;
import com.itau.chargeaccount.infrastructure.persistence.jpa.repository.ChargeJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adapter that implements ChargeRepository port using JPA.
 * Hexagonal architecture adapter converting domain calls to infrastructure calls.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChargeJpaAdapter implements ChargeRepository {

    private final ChargeJpaRepository jpaRepository;
    private final ChargeMapper mapper;

    @Override
    public void save(Charge charge) {
        log.debug("Saving charge: {}", charge.getChargeId());
        var entity = mapper.toEntity(charge);
        jpaRepository.save(entity);
        log.debug("Charge saved successfully: {}", charge.getChargeId());
    }

    @Override
    public void update(Charge charge) {
        log.debug("Updating charge: {}", charge.getChargeId());
        var entity = mapper.toEntity(charge);
        jpaRepository.save(entity);
        log.debug("Charge updated successfully: {}", charge.getChargeId());
    }

    @Override
    public Optional<Charge> findById(ChargeId chargeId) {
        log.debug("Finding charge by ID: {}", chargeId.getValue());
        return jpaRepository.findByChargeId(chargeId.getValue())
            .map(mapper::toDomain);
    }

    @Override
    public Optional<Charge> findByAccountId(String accountId) {
        log.debug("Finding charge by account ID: {}", accountId);
        return jpaRepository.findByAccountId(accountId)
            .map(mapper::toDomain);
    }
}

