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
public class FileConversionResult {

    private UUID sagaId;
    private String status;
    private String resultPath;
    private String errorMessage;
}
