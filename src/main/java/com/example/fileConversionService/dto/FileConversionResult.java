package com.example.fileConversionService.dto;

import com.example.fileConversionService.enums.SagaStatus;

import java.util.UUID;

public record FileConversionResult(
        UUID sagaId,
        SagaStatus status,
        String resultPath,
        String errorMessage
) {
}
