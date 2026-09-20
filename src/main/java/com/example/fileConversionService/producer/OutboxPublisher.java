package com.example.fileConversionService.producer;

import com.example.fileConversionService.domain.OutboxMessage;
import com.example.fileConversionService.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelayString = "${app.outbox.fixed-delay}")
    @Transactional
    public void publishPendingMessages() {
        List<OutboxMessage> pendingMessages = outboxRepository.findByProcessedFalseOrderByIdAsc();

        if (pendingMessages.isEmpty()) {
            return;
        }

        log.debug("Обнаружено {} неотправленных сообщений в таблице Outbox. Начинаем отправку...", pendingMessages.size());

        for (OutboxMessage message : pendingMessages) {
            try {
                kafkaTemplate.send(message.getTopic(), message.getPayload()).get();

                message.setProcessed(true);
                outboxRepository.save(message);

                log.info("Сообщение из Outbox ID {} успешно доставлено в топик Kafka: {}", message.getId(), message.getTopic());

            } catch (Exception e) {

                log.error("Сбой при отправке Outbox сообщения ID {}. Повтор в следующем цикле. Ошибка: {}",
                        message.getId(), e.getMessage());
                break;
            }
        }
    }
}
