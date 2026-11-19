package com.railflow.notification.repository;

import com.railflow.notification.entity.Notification;
import com.railflow.notification.entity.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByStatus(NotificationStatus status);

    List<Notification> findByRecipient(String recipient);
}
