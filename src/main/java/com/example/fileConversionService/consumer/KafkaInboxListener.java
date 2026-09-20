package com.example.fileConversionService.consumer;

import com.example.fileConversionService.dto.FileConversionCommand;
import com.example.fileConversionService.service.FileProcessingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaInboxListener {

    private final FileProcessingService fileProcessingService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "file-conversion-commands",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(String messageJson, Acknowledgment ack) {
        log.info("Получено новое сообщение из Kafka: {}", messageJson);

        try {
            FileConversionCommand command = objectMapper.readValue(messageJson, FileConversionCommand.class);
            fileProcessingService.processEvent(command);
            ack.acknowledge();
            log.info("Смещение (offset) успешно закоммичено в Kafka для messageId: {}", command.messageId());

        } catch (Exception e) {
            log.error("Критическая ошибка при обработке сообщения в консюмере. Оффсет НЕ коммитится. Ошибка: {}", e.getMessage());

        }
    }
}
