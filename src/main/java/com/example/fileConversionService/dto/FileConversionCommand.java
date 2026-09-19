package com.example.fileConversionService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileConversionCommand {
    private UUID messageId;
    private UUID sagaId;
    private String minioPath;
}

