package com.example.fileConversionService.converter;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ConverterRegistry {

    private final List<FileConverter> converters;

    public ConverterRegistry(List<FileConverter> converters) {
        this.converters = converters;
    }

    public Optional<FileConverter> getConverter(String extension) {
        return converters.stream()
                .filter(converter -> converter.supports(extension))
                .findFirst();
    }
}
