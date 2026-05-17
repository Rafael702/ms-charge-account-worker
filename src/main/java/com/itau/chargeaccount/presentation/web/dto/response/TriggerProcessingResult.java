package com.itau.chargeaccount.presentation.web.dto.response;

public class TriggerProcessingResult {
    long totalProcessed;
    long totalErrors;
    long durationSeconds;
    String startedAt;
    String finishedAt;
    String status;
}
