package com.example.fileConversionService.service;

import com.example.fileConversionService.converter.SagaStatus;
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
    public void saveResult(UUID messageId, FileConversionResult result) throws Exception {
        InboxMessage inbox = InboxMessage.builder()
                .messageId(messageId)
                .status(result.status())
                .processedAt(LocalDateTime.now())
                .build();
        inboxRepository.save(inbox);
        writeToOutbox("file-conversion-results", result);
    }

    private void writeToOutbox(String topic, FileConversionResult payloadDto) throws Exception {
        String jsonPayload = objectMapper.writeValueAsString(payloadDto);

        OutboxMessage outboxMessage = OutboxMessage.builder()
                .topic(topic)
                .payload(jsonPayload)
                .processed(false)
                .build();

        outboxRepository.save(outboxMessage);
    }
}
