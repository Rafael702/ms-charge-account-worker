package com.itau.chargeaccount.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA configuration for database layer.
 * Enables Spring Data JPA repositories and transaction management.
 */
@Configuration
@EnableJpaRepositories(
    basePackages = "com.itau.chargeaccount.infrastructure.persistence.jpa.repository"
)
@EnableTransactionManagement
public class JpaConfiguration {
    // Configuration is provided via Spring Boot auto-configuration
    // This class serves as an explicit marker for JPA setup
}

