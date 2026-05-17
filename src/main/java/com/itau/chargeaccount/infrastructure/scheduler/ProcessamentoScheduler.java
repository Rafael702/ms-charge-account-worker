package com.itau.chargeaccount.infrastructure.scheduler;

import com.itau.chargeaccount.application.port.FileReaderPort;
import com.itau.chargeaccount.application.service.ChargeProcessingApplicationService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Scheduler for batch processing of charge files.
 *
 * Responsibilities:
 * - Schedule file reading at specific times (04:00 AM daily)
 * - Orchestrate batch processing of charges
 * - Publish events for each charge
 * - Handle errors and retries
 * - Log processing metrics
 *
 * Implements Resilience4j patterns:
 * - @Retry: Automatic retries with exponential backoff
 * - @CircuitBreaker: Prevents cascade failures
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class ProcessamentoScheduler {

    private final FileReaderPort fileReaderPort;
    private final ChargeProcessingApplicationService chargeProcessingApplicationService;

    @Value("${app.file.path}")
    private String chargeFilePath;

    @Value("${app.batch.size}")
    private int batchSize;

    /**
     * Scheduled task that runs daily at 04:00 AM.
     * Reads the charge file and processes charges in batch.
     *
     * Cron expression: "0 4 * * *" = Every day at 04:00
     */
    @Scheduled(cron = "0 0 4 * * *", zone = "America/Sao_Paulo")
    @Retry(name = "schedulerRetry")
    @CircuitBreaker(name = "schedulerCircuitBreaker", fallbackMethod = "processingFallback")
    @Transactional
    public void processChargesDaily() {
        LocalDateTime startTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        log.info("========================================");
        log.info("Starting daily charge processing: {}", startTime.format(formatter));
        log.info("========================================");

        try {
            processChargeFile();
            log.info("========================================");
        } catch (Exception e) {
            log.error("Error during daily charge processing: {}", e.getMessage(), e);
            throw new RuntimeException("Daily charge processing failed", e);
        }
    }

    /**
     * Processes the charge file, reading and publishing charges in batches.
     *
     * @return Total number of charges processed
     */
    private long processChargeFile() {
        log.info("Reading charge file from: {}", chargeFilePath);

        AtomicInteger processedCount = new AtomicInteger(0);
        AtomicInteger batchCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        try {
            fileReaderPort.readChargeFile(chargeFilePath)
                    .peek(charge -> {
                        try {
                            processedCount.incrementAndGet();

                            // Process each charge
                            chargeProcessingApplicationService.processCharge(charge);

                            // Log progress every batch
                            if (processedCount.get() % batchSize == 0) {
                                batchCount.incrementAndGet();
                                log.info("Batch {} processed: {} charges total",
                                        batchCount.get(), processedCount.get());
                            }
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                            log.error("Error processing charge {}: {}",
                                    charge.getIdCharge(), e.getMessage());
                        }
                    })
                    .forEach(charge -> {
                        // Stream terminal operation
                    });

            log.info("File processing completed successfully");
            log.info("Total processed: {}, Errors: {}", processedCount.get(), errorCount.get());

            return processedCount.get();

        } catch (Exception e) {
            log.error("Error reading charge file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to read charge file", e);
        }
    }

    /**
     * Fallback method when circuit breaker is open or max retries exceeded.
     */
    public void processingFallback(Exception ex) {
        log.error("Circuit breaker opened or max retries exceeded for daily processing. Cause: {}",
                ex.getMessage());
        log.warn("Daily charge processing will be retried on next scheduled time");
    }

    /**
     * Manual trigger method for testing and on-demand processing.
     * Can be called via HTTP endpoint or directly.
     *
     * @return Number of charges processed
     */
    public long triggerManualProcessing() {
        log.info("Manual trigger for charge processing initiated");

        try {
            return processChargeFile();
        } catch (Exception e) {
            log.error("Error during manual charge processing: {}", e.getMessage(), e);
            throw new RuntimeException("Manual charge processing failed", e);
        }
    }
}

