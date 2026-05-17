package com.itau.chargeaccount.infrastructure.config;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for CSV file processing.
 * Provides CsvMapper bean for Jackson CSV operations.
 */
@Configuration
public class CsvConfiguration {

    @Bean
    public CsvMapper csvMapper() {
        return new CsvMapper();
    }
}

