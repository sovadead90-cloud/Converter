package com.example.fileConversionService.service;


import com.example.fileConversionService.converter.ConverterRegistry;
import com.example.fileConversionService.domain.InboxMessage;
import com.example.fileConversionService.domain.OutboxMessage;
import com.example.fileConversionService.dto.FileConversionCommand;
import com.example.fileConversionService.dto.FileConversionResult;
import com.example.fileConversionService.repository.InboxRepository;
import com.example.fileConversionService.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileProcessingService {

    private final InboxRepository inboxRepository;
    private final OutboxRepository outboxRepository;
    private final ConverterRegistry converterRegistry;
    private final MinioService minioService;
    private final ObjectMapper objectMapper;

    @Transactional
    public void processEvent(FileConversionCommand command) {
        UUID messageId = command.getMessageId();
        UUID sagaId = command.getSagaId();

        if (inboxRepository.existsById(messageId)) {
            log.warn("Дубликат сообщения! Событие с messageId {} уже было обработано ранее. Пропуск.", messageId);
            return;
        }

        try {
            log.info("Начало обработки конвертации для SAGA ID: {}, Файл: {}", sagaId, command.getMinioPath());

            String minioPath = command.getMinioPath();
            if (minioPath == null || !minioPath.contains(".")) {
                throw new IllegalArgumentException("Некорректный путь к файлу в хранилище");
            }

            InputStream fileStream = minioService.downloadFile(minioPath);
            String extension = minioPath.substring(minioPath.lastIndexOf(".") + 1);

            var converter = converterRegistry.getConverter(extension)
                    .orElseThrow(() -> new UnsupportedOperationException("Формат файла не поддерживается: " + extension));

            byte[] pdfBytes = converter.convert(fileStream);

            String targetPdfPath = "converted/" + sagaId + ".pdf";
            minioService.uploadFile(targetPdfPath, pdfBytes, "application/pdf");

            FileConversionResult successResult = FileConversionResult.builder()
                    .sagaId(sagaId)
                    .status("SUCCESS")
                    .resultPath(targetPdfPath)
                    .build();

            writeToOutbox("file-conversion-results", successResult);

            InboxMessage inbox = InboxMessage.builder()
                    .messageId(messageId)
                    .status("SUCCESS")
                    .processedAt(LocalDateTime.now())
                    .build();
            inboxRepository.save(inbox);

            log.info("Шаг SAGA [{}] успешно завершен. Результат записан в Outbox.", sagaId);

        } catch (Exception e) {
            log.error("Ошибка при обработке конвертации файла для SAGA ID [{}]: {}", sagaId, e.getMessage());

            try {
                FileConversionResult failedResult = FileConversionResult.builder()
                        .sagaId(sagaId)
                        .status("FAILED")
                        .errorMessage(e.getMessage())
                        .build();

                writeToOutbox("file-conversion-results", failedResult);

                InboxMessage inboxFailed = InboxMessage.builder()
                        .messageId(messageId)
                        .status("FAILED")
                        .processedAt(LocalDateTime.now())
                        .build();
                inboxRepository.save(inboxFailed);

            } catch (Exception ex) {
                log.error("Критический сбой записи аварийного статуса в БД", ex);
                throw new RuntimeException("Инфраструктурный сбой БД", ex);
            }
        }
    }

    private void writeToOutbox(String topic, Object payloadDto) throws Exception {
        String jsonPayload = objectMapper.writeValueAsString(payloadDto);

        OutboxMessage outboxMessage = OutboxMessage.builder()
                .topic(topic)
                .payload(jsonPayload)
                .processed(false)
                .build();

        outboxRepository.save(outboxMessage);
    }
}
