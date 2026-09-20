package com.example.fileConversionService.converter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
public class TxtToPdfConverter implements FileConverter {

    @Override
    public boolean supports(FileType fileType) {
        return FileType.TXT == fileType;
    }

    @Override
    public byte[] convert(InputStream inputStream) throws Exception {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(50, 750);

                String line;
                while ((line = reader.readLine()) != null) {
                    String cleanLine = line.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "")
                            .replace("\t", "    ")
                            .trim();

                    if (!cleanLine.isEmpty()) {
                        contentStream.showText(cleanLine);
                        contentStream.newLineAtOffset(0, -15);
                    }
                }
                contentStream.endText();
            }

            document.save(baos);
            return baos.toByteArray();
        }
    }

}
