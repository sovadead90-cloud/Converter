package com.example.fileConversionService.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record FileConversionCommand(
        UUID messageId,
        UUID sagaId,
        String minioPath
) {
}

