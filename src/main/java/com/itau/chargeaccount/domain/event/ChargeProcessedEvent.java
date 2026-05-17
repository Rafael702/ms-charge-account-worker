package com.itau.chargeaccount.domain.event;

import com.itau.chargeaccount.domain.valueobject.ChargeId;
import com.itau.chargeaccount.domain.valueobject.ProcessingResult;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Domain event triggered when a charge has been processed successfully.
 * This event is sent to the accounting system for recording.
 */
@Getter
public class ChargeProcessedEvent extends DomainEvent {
    private ChargeId chargeId;
    private String accountId;
    private String type;  // D = debit, C = credit
    private BigDecimal amount;
    private ProcessingResult result;

    public ChargeProcessedEvent(
            ChargeId chargeId,
            String accountId,
            String type,
            BigDecimal amount,
            ProcessingResult result) {
        super("ChargeProcessedEvent");
        this.chargeId = chargeId;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.result = result;
    }

    public ChargeProcessedEvent(){
        super("ChargeProcessedEvent");
    }

    @Override
    public String getEventName() {
        return "charge.processed";
    }
}

