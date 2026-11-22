package com.railflow.notification.kafka;

import com.railflow.notification.entity.OutboxEvent;
import com.railflow.notification.entity.OutboxStatus;
import com.railflow.notification.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class OutboxPublisherJob {

    private final OutboxEventRepository outboxEventRepository;
    private final Optional<KafkaTemplate<String, Object>> kafkaTemplate;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processOutboxEvents() {
        if (kafkaTemplate.isEmpty()) {
            return;
        }

        List<OutboxEvent> pendingEvents = outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("Found {} pending outbox events to publish to Kafka", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                String topic = event.getEventType().toLowerCase().replace("_", "-") + "-topic";
                kafkaTemplate.get().send(topic, event.getAggregateId(), event.getPayload());

                event.setStatus(OutboxStatus.PUBLISHED);
                event.setProcessedAt(LocalDateTime.now());
            } catch (Exception e) {
                log.error("Failed to publish outbox event {}: {}", event.getId(), e.getMessage());
                event.setStatus(OutboxStatus.FAILED);
            }
        }
        outboxEventRepository.saveAll(pendingEvents);
    }
}
