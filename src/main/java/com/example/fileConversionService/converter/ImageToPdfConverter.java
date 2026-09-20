package com.example.fileConversionService.converter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Component
public class ImageToPdfConverter implements FileConverter {

    @Override
    public boolean supports(FileType fileType) {
        return FileType.JPG == fileType || FileType.PNG == fileType;
    }

    @Override
    public byte[] convert(InputStream inputStream) throws Exception {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            BufferedImage bufferedImage = ImageIO.read(inputStream);
            if (bufferedImage == null) {
                throw new IllegalArgumentException("Не удалось прочитать файл изображения. Возможно, формат поврежден.");
            }

            PDPage page = new PDPage();
            document.addPage(page);

            PDImageXObject imageXObject = LosslessFactory.createFromImage(document, bufferedImage);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float width = page.getMediaBox().getWidth() - 40;
                float height = page.getMediaBox().getHeight() - 40;
                contentStream.drawImage(imageXObject, 20, 20, width, height);
            }

            document.save(baos);
            return baos.toByteArray();
        }
    }
}
