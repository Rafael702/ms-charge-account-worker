package com.itau.chargeaccount.application.usecase;

/**
 * Use case for manually triggering the charge file processing.
 *
 * Allows on-demand execution outside the scheduled window,
 * useful for testing, reprocessing and operational recovery.
 */
public interface TriggerChargeProcessingUseCase {

    /**
     * Triggers the charge file processing immediately.
     *
     * @param filePath optional custom file path; if null uses default configured path
     * @return result with total processed and error counts
     */
    TriggerProcessingResult execute(String filePath);

    /**
     * Immutable result of a manual trigger execution.
     */
    record TriggerProcessingResult(
            long totalProcessed,
            long totalErrors,
            long durationSeconds,
            String startedAt,
            String finishedAt,
            String status
    ) {}
}

