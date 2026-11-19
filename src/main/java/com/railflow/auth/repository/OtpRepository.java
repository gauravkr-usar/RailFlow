package com.railflow.auth.repository;

import com.railflow.auth.entity.OtpRecord;
import com.railflow.auth.entity.OtpRecord.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpRecord, Long> {

    Optional<OtpRecord> findTopByEmailAndPurposeAndIsUsedFalseOrderByIdDesc(
            String email, OtpPurpose purpose);

    @Modifying
    @Query("UPDATE OtpRecord o SET o.isUsed = true WHERE o.email = :email AND o.purpose = :purpose")
    void invalidateAllOtps(@Param("email") String email, @Param("purpose") OtpPurpose purpose);

    @Modifying
    @Query("DELETE FROM OtpRecord o WHERE o.expiresAt < :now")
    void deleteExpiredOtps(@Param("now") LocalDateTime now);
}
