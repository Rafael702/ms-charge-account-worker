package com.itau.chargeaccount.infrastructure.persistence.jpa.adapter;

import com.itau.chargeaccount.domain.entity.Charge;
import com.itau.chargeaccount.domain.valueobject.*;
import com.itau.chargeaccount.infrastructure.persistence.jpa.entity.ChargeEntity;
import com.itau.chargeaccount.infrastructure.persistence.jpa.mapper.ChargeMapper;
import com.itau.chargeaccount.infrastructure.persistence.jpa.repository.ChargeJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ChargeJpaAdapter.
 * Validates persistence adapter implementation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChargeJpaAdapter Tests")
class ChargeJpaAdapterTest {

    @Mock
    private ChargeJpaRepository jpaRepository;

    @Mock
    private ChargeMapper mapper;

    private ChargeJpaAdapter adapter;
    private Charge charge;
    private ChargeEntity entity;

    @BeforeEach
    void setUp() {
        adapter = new ChargeJpaAdapter(jpaRepository, mapper);

        charge = Charge.create(
            ChargeId.generate(),
            AccountId.valueOf("123456"),
            ChargeType.DEBIT,
            new BigDecimal("100.00")
        );

        entity = ChargeEntity.builder()
            .id(1L)
            .chargeId(charge.getChargeId().getValue())
            .accountId(charge.getAccountId().getValue())
            .chargeType(charge.getType().getCode())
            .amount(charge.getAmount())
            .chargeStatus(ChargeStatus.PENDING_VALIDATION.name())
            .chargeDate(LocalDateTime.now())
            .build();
    }

    @Test
    @DisplayName("Should save charge successfully")
    void shouldSaveCharge() {
        // Arrange
        when(mapper.toEntity(charge)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);

        // Act
        adapter.save(charge);

        // Assert
        verify(mapper, times(1)).toEntity(charge);
        verify(jpaRepository, times(1)).save(entity);
    }

    @Test
    @DisplayName("Should update charge successfully")
    void shouldUpdateCharge() {
        // Arrange
        when(mapper.toEntity(charge)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);

        // Act
        adapter.update(charge);

        // Assert
        verify(mapper, times(1)).toEntity(charge);
        verify(jpaRepository, times(1)).save(entity);
    }

    @Test
    @DisplayName("Should find charge by ID successfully")
    void shouldFindChargeById() {
        // Arrange
        ChargeId chargeId = charge.getChargeId();
        when(jpaRepository.findByChargeId(chargeId.getValue())).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(charge);

        // Act
        Optional<Charge> result = adapter.findById(chargeId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getChargeId()).isEqualTo(chargeId);
        verify(jpaRepository, times(1)).findByChargeId(chargeId.getValue());
        verify(mapper, times(1)).toDomain(entity);
    }

    @Test
    @DisplayName("Should return empty when charge not found by ID")
    void shouldReturnEmptyWhenChargeNotFoundById() {
        // Arrange
        ChargeId chargeId = charge.getChargeId();
        when(jpaRepository.findByChargeId(chargeId.getValue())).thenReturn(Optional.empty());

        // Act
        Optional<Charge> result = adapter.findById(chargeId);

        // Assert
        assertThat(result).isEmpty();
        verify(jpaRepository, times(1)).findByChargeId(chargeId.getValue());
        verify(mapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Should find charge by account ID successfully")
    void shouldFindChargeByAccountId() {
        // Arrange
        String accountId = charge.getAccountId().getValue();
        when(jpaRepository.findByAccountId(accountId)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(charge);

        // Act
        Optional<Charge> result = adapter.findByAccountId(accountId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getAccountId().getValue()).isEqualTo(accountId);
        verify(jpaRepository, times(1)).findByAccountId(accountId);
        verify(mapper, times(1)).toDomain(entity);
    }

    @Test
    @DisplayName("Should return empty when charge not found by account ID")
    void shouldReturnEmptyWhenChargeNotFoundByAccountId() {
        // Arrange
        String accountId = "nonexistent";
        when(jpaRepository.findByAccountId(accountId)).thenReturn(Optional.empty());

        // Act
        Optional<Charge> result = adapter.findByAccountId(accountId);

        // Assert
        assertThat(result).isEmpty();
        verify(jpaRepository, times(1)).findByAccountId(accountId);
        verify(mapper, never()).toDomain(any());
    }
}

