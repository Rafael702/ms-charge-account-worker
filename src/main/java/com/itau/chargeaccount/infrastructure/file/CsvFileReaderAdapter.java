package com.itau.chargeaccount.infrastructure.file;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.itau.chargeaccount.application.dto.ChargeDTO;
import com.itau.chargeaccount.application.port.FileReaderPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * CSV file reader adapter that implements FileReaderPort.
 * <p>
 * Reads large CSV files efficiently without loading entire file into memory.
 * Uses Jackson's CSV parser with streaming capability.
 * <p>
 * Supports both classpath and filesystem paths:
 * - classpath:file-test.csv  → src/main/resources/file-test.csv
 * - /absolute/path/file.csv  → filesystem absolute path
 * - ./relative/path/file.csv → filesystem relative path
 * <p>
 * Expected CSV format:
 * chargeId,accountId,chargeType,amount
 * CHARGE-001,ACCOUNT-123,D,100.00
 * CHARGE-002,ACCOUNT-456,C,200.50
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CsvFileReaderAdapter implements FileReaderPort {

    private final CsvMapper csvMapper;
    private final ResourceLoader resourceLoader;

    /**
     * Reads a charge file and returns a stream of DTOs.
     * Uses Spring ResourceLoader to support classpath: and filesystem paths.
     * Uses Jackson's streaming parser for memory efficiency.
     *
     * @param filePath Path to the CSV file (supports classpath: prefix)
     * @return Stream of ChargeDTO objects
     * @throws RuntimeException if file cannot be read
     */
    @Override
    public Stream<ChargeDTO> readChargeFile(String filePath) {
        log.info("Starting to read charge file: {}", filePath);

        try {
            Resource resource = resourceLoader.getResource(filePath);

            if (!resource.exists()) {
                log.error("File not found: {}", filePath);
                throw new RuntimeException("File not found: " + filePath);
            }

            if (!resource.isReadable()) {
                log.error("File not readable: {}", filePath);
                throw new RuntimeException("File not readable: " + filePath);
            }

            // Define CSV schema with headers
            CsvSchema schema = CsvSchema.emptySchema()
                    .withHeader()
                    .withColumnSeparator(',')
                    .withSkipFirstDataRow(false);

            InputStream inputStream = resource.getInputStream();

            // Create an iterator for streaming CSV parsing
            MappingIterator<ChargeCSVRecord> iterator = csvMapper
                    .readerFor(ChargeCSVRecord.class)
                    .with(schema)
                    .readValues(inputStream);

            log.debug("CSV file opened successfully: {}", filePath);

            // Convert iterator to stream with proper resource management
            return StreamSupport.stream(
                            Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                            false
                    ).map(this::convertToChargeDTO)
                    .onClose(() -> {
                        try {
                            iterator.close();
                            inputStream.close();
                            log.debug("CSV file stream closed");
                        } catch (IOException e) {
                            log.error("Error closing CSV stream", e);
                        }
                    });
        } catch (Exception e) {
            log.error("Error reading CSV file {}: {}", filePath, e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private ChargeDTO convertToChargeDTO(ChargeCSVRecord record) {
        return ChargeDTO.builder()
                .idCharge(record.getChargeId())
                .idAccount(record.getAccountId())
                .type(record.getChargeType())
                .amount(new BigDecimal(record.getAmount()))
                .build();
    }
}

