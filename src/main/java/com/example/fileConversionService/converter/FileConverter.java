package com.example.fileConversionService.converter;

import com.example.fileConversionService.enums.FileType;

import java.io.InputStream;

public interface FileConverter {

    boolean supports(FileType fileType);

    byte[] convert(InputStream inputStream) throws Exception;
}
