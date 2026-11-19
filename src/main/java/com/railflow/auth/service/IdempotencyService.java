package com.railflow.auth.service;

import com.railflow.auth.entity.IdempotencyRecord;
import com.railflow.auth.entity.IdempotencyRecord.IdempotencyStatus;
import com.railflow.auth.repository.IdempotencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private static final String REDIS_KEY_PREFIX = "idempotency:";
    private static final Duration REDIS_TTL = Duration.ofHours(24);
    private static final Duration RECORD_EXPIRY = Duration.ofDays(7);

    private final IdempotencyRepository idempotencyRepository;
    private final StringRedisTemplate redisTemplate;

    /**
     * Check if an idempotency key already exists and has completed.
     * If completed, return the cached response.
     * If PROCESSING, return empty (caller should wait or return 409).
     * If not found, create a PROCESSING record and return empty.
     */
    @Transactional
    public Optional<String> checkAndCreate(String key, String requestHash) {
        // Fast Redis lookup first
        String redisKey = REDIS_KEY_PREFIX + key;
        String cachedResponse = redisTemplate.opsForValue().get(redisKey);
        if (cachedResponse != null) {
            log.debug("Idempotency cache hit for key: {}", key);
            return Optional.of(cachedResponse);
        }

        // Fall back to PostgreSQL
        Optional<IdempotencyRecord> existing = idempotencyRepository.findByIdempotencyKey(key);

        if (existing.isPresent()) {
            IdempotencyRecord record = existing.get();
            if (record.getStatus() == IdempotencyStatus.COMPLETED && record.getResponse() != null) {
                // Repopulate Redis cache
                redisTemplate.opsForValue().set(redisKey, record.getResponse(), REDIS_TTL);
                return Optional.of(record.getResponse());
            }
            // PROCESSING or FAILED — return empty to let caller handle
            return Optional.empty();
        }

        // Create new PROCESSING record
        IdempotencyRecord newRecord = IdempotencyRecord.builder()
                .idempotencyKey(key)
                .requestHash(requestHash)
                .status(IdempotencyStatus.PROCESSING)
                .expiresAt(LocalDateTime.now().plus(RECORD_EXPIRY))
                .build();
        idempotencyRepository.save(newRecord);
        return Optional.empty();
    }

    @Transactional
    public void complete(String key, String response) {
        idempotencyRepository.updateStatusAndResponse(key, IdempotencyStatus.COMPLETED, response);
        // Store in Redis for fast future lookups
        String redisKey = REDIS_KEY_PREFIX + key;
        redisTemplate.opsForValue().set(redisKey, response, REDIS_TTL);
        log.debug("Idempotency key completed and cached: {}", key);
    }

    @Transactional
    public void fail(String key) {
        idempotencyRepository.updateStatus(key, IdempotencyStatus.FAILED);
        log.debug("Idempotency key marked as failed: {}", key);
    }
}
