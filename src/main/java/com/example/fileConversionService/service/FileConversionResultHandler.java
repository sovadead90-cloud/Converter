package com.example.fileConversionService.service;

import com.example.fileConversionService.domain.InboxMessage;
import com.example.fileConversionService.domain.OutboxMessage;
import com.example.fileConversionService.dto.FileConversionResult;
import com.example.fileConversionService.repository.InboxRepository;
import com.example.fileConversionService.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileConversionResultHandler {

    private final InboxRepository inboxRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional(rollbackFor = Exception.class)
    public void saveSuccessResults(UUID messageId, UUID sagaId, String targetPdfPath) throws Exception {
        FileConversionResult successResult = new FileConversionResult(
                sagaId,
                "SUCCESS",
                targetPdfPath,
                null
        );

        writeToOutbox("file-conversion-results", successResult);

        InboxMessage inbox = InboxMessage.builder()
                .messageId(messageId)
                .status("SUCCESS")
                .processedAt(LocalDateTime.now())
                .build();
        inboxRepository.save(inbox);
    }

    @Transactional
    public void saveFailedResults(UUID messageId, UUID sagaId, String errorMessage) {
        try {
            FileConversionResult failedResult = new FileConversionResult(
                    sagaId,
                    "FAILED",
                    null,
                    errorMessage
            );

            writeToOutbox("file-conversion-results", failedResult);

            InboxMessage inboxFailed = InboxMessage.builder()
                    .messageId(messageId)
                    .status("FAILED")
                    .processedAt(LocalDateTime.now())
                    .build();
            inboxRepository.save(inboxFailed);
        } catch (Exception ex) {
            log.error("Критический сбой транзакции при записи аварийного статуса SAGA в БД для messageId: {}"
                    , messageId, ex);
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
