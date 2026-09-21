package com.example.fileConversionService.dto;

import java.util.UUID;

public record FileConversionResult(
        UUID sagaId,
        String status,
        String resultPath,
        String errorMessage
) {
}
