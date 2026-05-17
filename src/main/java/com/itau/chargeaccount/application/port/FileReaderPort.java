package com.itau.chargeaccount.application.port;

import com.itau.chargeaccount.application.dto.ChargeDTO;

import java.util.stream.Stream;

/**
 * Port (Interface) for reading charge files.
 * Implements the Hexagonal Architecture port pattern.
 *
 * The concrete implementation (Adapter) will be in the Infrastructure layer (ex: CsvFileReaderAdapter).
 */
public interface FileReaderPort {

    /**
     * Reads a charge file and returns a stream of DTOs.
     * Using stream allows asynchronous and efficient processing of large files.
     */
    Stream<ChargeDTO> readChargeFile(String filePath);
}

