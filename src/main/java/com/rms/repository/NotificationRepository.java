package com.rms.repository;

import com.rms.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** All notifications for a user, newest first. */
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    /** Unread notifications for a user, newest first. */
    List<Notification> findByRecipientIdAndReadFalseOrderByCreatedAtDesc(Long recipientId);

    /** Unread count - for the notification badge in the navbar. */
    long countByRecipientIdAndReadFalse(Long recipientId);
}
