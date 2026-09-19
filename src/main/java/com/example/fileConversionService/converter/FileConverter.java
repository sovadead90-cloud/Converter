package com.example.fileConversionService.converter;

import java.io.InputStream;

public interface FileConverter {

    boolean supports(String fileExtension);

    byte[] convert(InputStream inputStream) throws Exception;
}
