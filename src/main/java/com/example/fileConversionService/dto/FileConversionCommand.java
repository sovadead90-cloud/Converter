package com.example.fileConversionService.dto;

import java.util.UUID;

public record FileConversionCommand(
        UUID messageId,
        UUID sagaId,
        String minioPath
) {
}

