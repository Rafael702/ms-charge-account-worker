package com.itau.chargeaccount.domain.event;

import com.itau.chargeaccount.domain.valueobject.ChargeId;
import com.itau.chargeaccount.domain.valueobject.AccountStatus;
import lombok.Getter;

/**
 * Domain event triggered when the account has been validated by the account system.
 * This is the expected response when we publish ChargeReceivedEvent.
 */
@Getter
public class AccountValidatedEvent extends DomainEvent {
    private final ChargeId chargeId;
    private final String accountId;
    private final AccountStatus accountStatus;

    public AccountValidatedEvent(ChargeId chargeId, String accountId, AccountStatus accountStatus) {
        super("AccountValidatedEvent");
        this.chargeId = chargeId;
        this.accountId = accountId;
        this.accountStatus = accountStatus;
    }

    @Override
    public String getEventName() {
        return "account.validated";
    }
}

