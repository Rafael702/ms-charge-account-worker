package com.itau.chargeaccount.domain.service;

import com.itau.chargeaccount.domain.entity.Charge;
import com.itau.chargeaccount.domain.valueobject.AccountStatus;
import org.springframework.stereotype.Service;

/**
 * Domain Service that encapsulates the charge processing logic.
 *
 * In DDD, a Domain Service is used when:
 * - The logic involves multiple entities or aggregates
 * - The logic is too complex to fit in a single entity
 * - It doesn't make sense conceptually for an entity to know how to do it
 *
 * In this case, processing involves:
 * 1. Validating the account status (Account entity is not a separate aggregate in this project)
 * 2. Updating the charge
 * 3. Generating events
 */
@Service
public class ChargeProcessingDomainService {

    /**
     * Processes a charge based on account status.
     *
     * Processing involves:
     * 1. Registering the account status in the charge
     * 2. Executing the processing logic (validate if can/should process)
     * 3. The charge is responsible for updating its own state
     */
    public void processChargeWithAccountStatus(Charge charge, AccountStatus accountStatus) {
        if (charge == null) {
            throw new IllegalArgumentException("Charge cannot be null");
        }
        if (accountStatus == null) {
            throw new IllegalArgumentException("AccountStatus cannot be null");
        }

        // Register account status in the charge (domain logic validation)
        charge.startAccountValidation(accountStatus);

        // Process the charge (apply business rules)
        charge.process();
    }
}

