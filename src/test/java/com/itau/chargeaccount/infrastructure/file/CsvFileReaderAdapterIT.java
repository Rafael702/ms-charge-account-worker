package com.itau.chargeaccount.infrastructure.file;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.itau.chargeaccount.application.dto.ChargeDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for CsvFileReaderAdapter.
 * Tests CSV file reading with temporary test files.
 */
@Slf4j
@DisplayName("CsvFileReaderAdapter Integration Tests")
class CsvFileReaderAdapterIT {

    private CsvFileReaderAdapter csvFileReaderAdapter;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        csvFileReaderAdapter = new CsvFileReaderAdapter(new CsvMapper(), new DefaultResourceLoader());
    }

    @Test
    @DisplayName("Should read valid CSV file successfully")
    void shouldReadValidCsvFileSuccessfully() throws Exception {
        // Given
        File csvFile = createTestCsvFile(tempDir, "test_charges.csv",
                "chargeId,accountId,chargeType,amount\n" +
                "CHARGE-001,ACCOUNT-123,D,100.00\n" +
                "CHARGE-002,ACCOUNT-456,C,200.50\n"
        );

        // When
        List<ChargeDTO> charges = csvFileReaderAdapter.readChargeFile("file:" + csvFile.getAbsolutePath())
                .collect(Collectors.toList());

        // Then
        assertThat(charges).hasSize(2);
        assertThat(charges.get(0).getIdCharge()).isEqualTo("CHARGE-001");
        assertThat(charges.get(0).getIdAccount()).isEqualTo("ACCOUNT-123");
        assertThat(charges.get(0).getType()).isEqualTo("D");
        assertThat(charges.get(0).getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100.00));

        log.info("CSV file read successfully with {} charges", charges.size());
    }

    @Test
    @DisplayName("Should handle large CSV file with streaming")
    void shouldHandleLargeCsvFileWithStreaming() throws Exception {
        // Given - Create a CSV with many records
        StringBuilder csvContent = new StringBuilder("chargeId,accountId,chargeType,amount\n");
        for (int i = 1; i <= 1000; i++) {
            csvContent.append(String.format("CHARGE-%06d,ACCOUNT-%06d,D,%d.00\n", i, i * 2, i));
        }

        File csvFile = createTestCsvFile(tempDir, "large_charges.csv", csvContent.toString());

        // When
        List<ChargeDTO> charges = csvFileReaderAdapter.readChargeFile("file:" + csvFile.getAbsolutePath())
                .collect(Collectors.toList());

        // Then
        assertThat(charges).hasSize(1000);
        assertThat(charges.get(0).getIdCharge()).isEqualTo("CHARGE-000001");
        assertThat(charges.get(999).getIdCharge()).isEqualTo("CHARGE-001000");

        log.info("Large CSV file processed successfully with {} charges", charges.size());
    }

    @Test
    @DisplayName("Should handle mixed debit and credit charges")
    void shouldHandleMixedDebitAndCreditCharges() throws Exception {
        // Given
        File csvFile = createTestCsvFile(tempDir, "mixed_charges.csv",
                "chargeId,accountId,chargeType,amount\n" +
                "CHARGE-001,ACCOUNT-123,D,100.00\n" +
                "CHARGE-002,ACCOUNT-123,C,50.25\n" +
                "CHARGE-003,ACCOUNT-456,D,200.75\n"
        );

        // When
        List<ChargeDTO> charges = csvFileReaderAdapter.readChargeFile("file:" + csvFile.getAbsolutePath())
                .collect(Collectors.toList());

        // Then
        assertThat(charges).hasSize(3);
        assertThat(charges.stream().filter(c -> "D".equals(c.getType())).count()).isEqualTo(2);
        assertThat(charges.stream().filter(c -> "C".equals(c.getType())).count()).isEqualTo(1);

        log.info("Mixed charges processed successfully");
    }

    @Test
    @DisplayName("Should throw exception for non-existent file")
    void shouldThrowExceptionForNonExistentFile() {
        // Given
        String nonExistentPath = "file:/non/existent/file.csv";

        // When & Then
        assertThatThrownBy(() -> csvFileReaderAdapter.readChargeFile(nonExistentPath)
                .toList())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("File not found");

        log.info("Non-existent file error handled correctly");
    }

    @Test
    @DisplayName("Should handle CSV with different decimal places")
    void shouldHandleCsvWithDifferentDecimalPlaces() throws Exception {
        // Given
        File csvFile = createTestCsvFile(tempDir, "decimal_charges.csv",
                "chargeId,accountId,chargeType,amount\n" +
                "CHARGE-001,ACCOUNT-123,D,100\n" +
                "CHARGE-002,ACCOUNT-456,C,200.5\n" +
                "CHARGE-003,ACCOUNT-789,D,300.50\n" +
                "CHARGE-004,ACCOUNT-001,C,400.125\n"
        );

        // When
        List<ChargeDTO> charges = csvFileReaderAdapter.readChargeFile("file:" + csvFile.getAbsolutePath())
                .collect(Collectors.toList());

        // Then
        assertThat(charges).hasSize(4);
        assertThat(charges.get(0).getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(charges.get(1).getAmount()).isEqualByComparingTo(BigDecimal.valueOf(200.5));
        assertThat(charges.get(2).getAmount()).isEqualByComparingTo(BigDecimal.valueOf(300.50));
        assertThat(charges.get(3).getAmount()).isEqualByComparingTo(BigDecimal.valueOf(400.125));

        log.info("CSV with different decimal places processed successfully");
    }

    /**
     * Helper method to create a temporary test CSV file
     */
    private File createTestCsvFile(Path tempDir, String fileName, String content) throws Exception {
        File csvFile = tempDir.resolve(fileName).toFile();
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write(content);
        }
        return csvFile;
    }
}

