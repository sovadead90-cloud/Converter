package com.example.fileConversionService.service;

import com.example.fileConversionService.converter.FileConverter;
import com.example.fileConversionService.converter.FileType;
import com.example.fileConversionService.dto.FileConversionCommand;
import com.example.fileConversionService.repository.InboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileProcessingService {

    private final InboxRepository inboxRepository;
    private final List<FileConverter> converters;
    private final MinioService minioService;
    private final FileConversionResultHandler resultHandler;

    public void processEvent(FileConversionCommand command) {
        UUID messageId = command.messageId();
        UUID sagaId = command.sagaId();
        String minioPath = command.minioPath();

        if (inboxRepository.existsById(messageId)) {
            log.warn("Дубликат сообщения! Событие с messageId {} уже было обработано ранее. Пропуск.", messageId);
            return;
        }

        try {
            log.info("Начало обработки конвертации для SAGA ID: {}, Файл: {}", sagaId, minioPath);

            if (minioPath == null || !minioPath.contains(".")) {
                throw new IllegalArgumentException("Некорректный путь к файлу в хранилище");
            }

            InputStream fileStream = minioService.downloadFile(minioPath);

            String extension = minioPath.substring(minioPath.lastIndexOf(".") + 1);
            FileType fileType = FileType.fromExtension(extension);

            FileConverter converter = converters.stream()
                    .filter(c -> c.supports(fileType))
                    .findFirst()
                    .orElseThrow(() -> new UnsupportedOperationException("Формат файла не поддерживается: " + fileType));

            byte[] pdfBytes = converter.convert(fileStream);

            String targetPdfPath = "converted/" + sagaId + ".pdf";

            minioService.uploadFile(targetPdfPath, pdfBytes, "application/pdf");

            resultHandler.saveSuccessResults(messageId, sagaId, targetPdfPath);

            log.info("Шаг SAGA [{}] успешно завершен. Результат зафиксирован.", sagaId);

        } catch (Exception e) {
            log.error("Ошибка при обработке конвертации файла для SAGA ID [{}]: {}", sagaId, e.getMessage());

            resultHandler.saveFailedResults(messageId, sagaId, e.getMessage());
        }
    }
}
