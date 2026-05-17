package com.itau.chargeaccount.domain.event;

import com.itau.chargeaccount.domain.valueobject.ChargeId;
import lombok.*;

import java.math.BigDecimal;

/**
 * Domain event triggered when a new charge is received.
 */
@Getter
public class ChargeReceivedEvent extends DomainEvent {
    private ChargeId chargeId;
    private String accountId;
    private String type;  // D = debit, C = credit
    private BigDecimal amount;

    public ChargeReceivedEvent(ChargeId chargeId, String accountId, String type, BigDecimal amount) {
        super("ChargeReceivedEvent");
        this.chargeId = chargeId;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
    }

    public ChargeReceivedEvent() {
        super("ChargeReceivedEvent");
    }

    @Override
    public String getEventName() {
        return "charge.received";
    }
}

