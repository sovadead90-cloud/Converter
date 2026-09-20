package com.example.fileConversionService.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record FileConversionResult(
        UUID sagaId,
        String status,
        String resultPath,
        String errorMessage
) {
}
