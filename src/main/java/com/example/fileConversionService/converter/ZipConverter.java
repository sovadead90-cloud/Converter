package com.example.fileConversionService.converter;

import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class ZipConverter implements FileConverter {

    private final ApplicationContext applicationContext;

    public ZipConverter(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean supports(FileType fileType) {
        return FileType.ZIP == fileType;
    }

    @Override
    public byte[] convert(InputStream inputStream) throws Exception {
        List<byte[]> convertedPdfPieces = new ArrayList<>();

        ConverterRegistry registry = applicationContext.getBean(ConverterRegistry.class);

        try (ZipInputStream zis = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    zis.closeEntry();
                    continue;
                }

                String entryName = entry.getName();
                if (!entryName.contains(".")) {
                    zis.closeEntry();
                    continue;
                }

                String extension = entryName.substring(entryName.lastIndexOf(".") + 1);

                if ("zip".equalsIgnoreCase(extension)) {
                    zis.closeEntry();
                    continue;
                }

                FileType internalFileType = FileType.fromExtension(extension);
                var internalConverterOpt = registry.getConverter(internalFileType);

                if (internalConverterOpt.isPresent()) {
                    ByteArrayOutputStream entryBuffer = new ByteArrayOutputStream();
                    zis.transferTo(entryBuffer);

                    try (InputStream entryStream = new ByteArrayInputStream(entryBuffer.toByteArray())) {
                        byte[] pdfBytes = internalConverterOpt.get().convert(entryStream);
                        convertedPdfPieces.add(pdfBytes);
                    }
                }
                zis.closeEntry();
            }
        }

        if (convertedPdfPieces.isEmpty()) {
            throw new IllegalArgumentException(
                    "В ZIP архиве не обнаружено файлов поддерживаемого формата (TXT, PNG, JPG)");
        }

        return mergePdfDocuments(convertedPdfPieces);
    }

    private byte[] mergePdfDocuments(List<byte[]> pdfList) throws IOException {
        PDFMergerUtility pdfMerger = new PDFMergerUtility();
        ByteArrayOutputStream mergedOutput = new ByteArrayOutputStream();
        pdfMerger.setDestinationStream(mergedOutput);

        for (byte[] pdfBytes : pdfList) {
            pdfMerger.addSource(new RandomAccessReadBuffer(pdfBytes));
        }

        pdfMerger.mergeDocuments(null);
        return mergedOutput.toByteArray();
    }
}
