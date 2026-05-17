package com.itau.chargeaccount.presentation.web;
import com.itau.chargeaccount.application.usecase.TriggerChargeProcessingUseCase;
import com.itau.chargeaccount.application.usecase.TriggerChargeProcessingUseCase.TriggerProcessingResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
/**
 * REST controller for manual triggering of charge file processing.
 *
 * Driving adapter (Primary Adapter) in the Hexagonal Architecture.
 * Receives HTTP requests and delegates to the Application layer via Use Cases.
 *
 * Intended for:
 * - Functional and integration testing
 * - Operational recovery (reprocessing outside the scheduled window)
 * - On-demand execution in non-production environments
 */
@Slf4j
@RestController
@RequestMapping("/processing")
@RequiredArgsConstructor
public class ChargeProcessingController {
    private final TriggerChargeProcessingUseCase triggerChargeProcessingUseCase;
    /**
     * POST /processing/trigger
     *
     * Triggers file processing immediately.
     * Optionally accepts a custom file path via query parameter.
     */
    @PostMapping("/trigger")
    public ResponseEntity<TriggerProcessingResult> triggerProcessing(
            @RequestParam(required = false) String filePath
    ) {
        log.info("Manual trigger requested via HTTP. FilePath: {}", filePath);
        TriggerProcessingResult result = triggerChargeProcessingUseCase.execute(filePath);
        if ("SUCCESS".equals(result.status())) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
}
