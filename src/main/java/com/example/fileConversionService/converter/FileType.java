package com.example.fileConversionService.converter;

import java.util.Arrays;

public enum FileType {
    TXT,
    PNG,
    JPG,
    ZIP;

    public static FileType fromExtension(String extension) {
        if (extension == null) {
            throw new IllegalArgumentException("Extension cannot be null");
        }
        String cleanExt = extension.trim().toUpperCase();
        if ("JPEG".equals(cleanExt)) {
            cleanExt = "JPG";
        }

        String finalCleanExt = cleanExt;
        return Arrays.stream(values())
                .filter(type -> type.name().equals(finalCleanExt))
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException("Unsupported file type: " + finalCleanExt));
    }
}
