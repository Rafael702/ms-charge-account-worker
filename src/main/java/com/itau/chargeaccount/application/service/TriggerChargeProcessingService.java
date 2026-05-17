package com.itau.chargeaccount.application.service;

import com.itau.chargeaccount.application.usecase.TriggerChargeProcessingUseCase;
import com.itau.chargeaccount.infrastructure.scheduler.ProcessamentoScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Application service that implements the manual trigger use case.
 *
 * Delegates to {@link ProcessamentoScheduler#triggerManualProcessing()} to reuse
 * the existing file reading and charge processing logic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TriggerChargeProcessingService implements TriggerChargeProcessingUseCase {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ProcessamentoScheduler processamentoScheduler;

    @Override
    public TriggerProcessingResult execute(String filePath) {
        LocalDateTime startTime = LocalDateTime.now();
        log.info("Manual trigger initiated via use case. FilePath: {}", filePath);

        try {
            long totalProcessed = processamentoScheduler.triggerManualProcessing();

            LocalDateTime endTime = LocalDateTime.now();
            long duration = java.time.temporal.ChronoUnit.SECONDS.between(startTime, endTime);

            return new TriggerProcessingResult(
                    totalProcessed,
                    0L,
                    duration,
                    startTime.format(FORMATTER),
                    endTime.format(FORMATTER),
                    "SUCCESS"
            );

        } catch (Exception e) {
            LocalDateTime endTime = LocalDateTime.now();
            long duration = java.time.temporal.ChronoUnit.SECONDS.between(startTime, endTime);
            log.error("Manual trigger failed: {}", e.getMessage(), e);

            return new TriggerProcessingResult(
                    0L,
                    1L,
                    duration,
                    startTime.format(FORMATTER),
                    endTime.format(FORMATTER),
                    "ERROR"
            );
        }
    }
}

