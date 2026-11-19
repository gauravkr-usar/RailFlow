package com.railflow.auth.repository;

import com.railflow.auth.entity.IdempotencyRecord;
import com.railflow.auth.entity.IdempotencyRecord.IdempotencyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface IdempotencyRepository extends JpaRepository<IdempotencyRecord, Long> {

    Optional<IdempotencyRecord> findByIdempotencyKey(String idempotencyKey);

    @Modifying
    @Query("UPDATE IdempotencyRecord i SET i.status = :status, i.response = :response WHERE i.idempotencyKey = :key")
    void updateStatusAndResponse(
            @Param("key") String key,
            @Param("status") IdempotencyStatus status,
            @Param("response") String response);

    @Modifying
    @Query("UPDATE IdempotencyRecord i SET i.status = :status WHERE i.idempotencyKey = :key")
    void updateStatus(@Param("key") String key, @Param("status") IdempotencyStatus status);

    @Modifying
    @Query("DELETE FROM IdempotencyRecord i WHERE i.expiresAt < :now")
    void deleteExpiredRecords(@Param("now") LocalDateTime now);
}
